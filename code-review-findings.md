# Code review: REST to key-generation path

**Scope:** code reachable from `fr.lis.ikeyplus.rest.Application`. That is the Javalin routes `/health` and `/identification-key`, then `RestHandler`, SDD parsing, `IdentificationKeyGenerator` and `SingleAccessKeyTreeDumper`.

**Mode:** high effort, tuned for recall. The findings are unverified candidates, ranked most severe first. Javalin behaviour (findings 3 and 9) was checked against the javalin 7.2.4 sources.

---

## 1. Stored XSS in HTML outputs

**Where:** `ikey-plus-api/src/main/java/fr/lis/ikeyplus/IO/SingleAccessKeyTreeDumper.java`, lines 615, 852 and 942. Leaf rendering is at lines 1082 and 1234.

**Problem:** the HTML outputs (tree, flat and interactive) write several values into the text/html response without escaping them:
- the header
- the dataset label
- taxon names
- image `href`s

Only character names and states are escaped.

**Failure scenario:**
1. An attacker hosts an SDD file. Its dataset `<Label>`, or a taxon `<Label>`, contains `&lt;script&gt;...&lt;/script&gt;`. Alternatively, an image `MediaObject` has `href="javascript:..."`.
2. The SAX parser decodes the entities back to `<script>`.
3. A victim opens `/identification-key?format=html&verbosity=h&sddURL=http://attacker/x.sdd`.
4. The script runs in the IKey+ origin.

## 2. Server-side request forgery (SSRF) on `sddURL`

**Where:** `ikey-plus-rest/src/main/java/fr/lis/ikeyplus/rest/RestHandler.java`, line 51.

**Problem:** the server fetches any http(s) URL with `URL.openStream()`, and nothing limits that fetch:
- no host or IP allow-list
- no connect or read timeout
- no response-size cap
- redirects to the same protocol are followed

**Failure scenario:**
- `sddURL=http://169.254.169.254/...` or `http://localhost:<port>/admin` makes the server probe internal services. The response time and the 500 vs 400 status leak the results.
- An endpoint that never responds keeps a Jetty worker thread busy indefinitely.
- A multi-GB response body is parsed in full.

## 3. The SAXException handler returns 500 instead of 400

**Where:** `ikey-plus-rest/src/main/java/fr/lis/ikeyplus/rest/ExceptionMapper.java`, line 13.

**Problem:** the handler throws `BadRequestResponse` from inside the exception handler. Javalin's `ExceptionMapper.handleExceptionSafely` catches exceptions thrown by a handler, logs `Exception handler ... threw an error`, and responds with `InternalServerErrorResponse`.

**Failure scenario:** a malformed XML file, or one with a DOCTYPE (blocked by `disallow-doctype-decl`), gets HTTP 500 instead of 400.

**Fix:** use `ctx.status(400).result("Bad request")` instead of throwing.

## 4. Unchecked exceptions from bad input become 500 errors

**Where:** `ikey-plus-api/src/main/java/fr/lis/ikeyplus/IO/SDDContentHandler.java`, line 328.

**Problem:** malformed SDD input raises unchecked exceptions that no exception handler maps:

| Code                                        | Exception when                                             |
|---------------------------------------------|------------------------------------------------------------|
| `attributes.getValue("type").equals(...)`   | `Measure` has no `type` attribute (NPE)                    |
| `Double.valueOf(...)`                       | the value is not a number (NumberFormatException)          |
| `getValue("context").equals(...)`, line 370 | `Rating` has no `context` attribute (NPE)                  |
| `(CategoricalCharacter)` cast               | the character `ref` is unknown (ClassCastException or NPE) |

The `IOException` that `openStream` throws when the URL returns 404 is not mapped either.

**Failure scenario:** `<Measure value="abc" type="Min"/>`, or an `sddURL` that returns 404, produces HTTP 500 with a stack trace. These are client or upstream errors and should be 400 or 502.

## 5. SDD output escapes entities twice

**Where:** `ikey-plus-api/src/main/java/fr/lis/ikeyplus/IO/SingleAccessKeyTreeDumper.java`, line 202. The same code is at lines 208, 238, 244, 279 and 286.

**Problem:** the code escapes `>`, then `<`, then `&`. The last step escapes the entities the first two just produced. The `TaxonName` label at line 91 uses the correct order (`&` first).

**Failure scenario:** a state named `length < 5 mm` is emitted as `length &amp;lt; 5 mm`, and SDD consumers display `length &lt; 5 mm`.

## 6. SDD output is malformed XML when the root has no children

**Where:** `ikey-plus-api/src/main/java/fr/lis/ikeyplus/IO/SingleAccessKeyTreeDumper.java`, line 303.

**Problem:** `</Leads>` is always emitted. `<Leads>` is only opened when `counter == 2`, which means the root has at least one child. Separately, the key `<Label>` at line 164 writes `tree2dump.getLabel()` without escaping.

**Failure scenario:**
- `format=sdd` on a dataset with a single taxon, or one with no discriminating characters, returns XML with an unmatched `</Leads>`.
- A dataset label containing `&` or `<` also breaks the XML.

## 7. The quantitative split threshold uses wrong interval pairs

**Where:** `ikey-plus-api/src/main/java/fr/lis/ikeyplus/services/IdentificationKeyGenerator.java`, line 367.

**Problem:** `splitQuantitativeCharacter` sorts the flat list of min and max values. It then treats indices (2j, 2j+1) as one taxon's (min, max) pair, but the sort destroyed that pairing. The pairs are also misaligned when a taxon has only a min or only a max (lines 425-430).

**Failure scenario:** taxa A=[1,10] and B=[2,3] sort to [1,2,3,10], so the pairs become (1,2) and (3,10). The threshold is chosen from these invented intervals, which skews the quantitative scores and the structure of the key.

## 8. A shared SAXParserFactory is used from several threads

**Where:** `ikey-plus-rest/src/main/java/fr/lis/ikeyplus/rest/RestHandler.java`, line 38.

**Problem:** one static `SDDSaxParser` holds one `SAXParserFactory`. Every Jetty request thread calls its `newSAXParser()` concurrently, and the JAXP docs say `SAXParserFactory` is not guaranteed to be thread-safe.

**Failure scenario:** under concurrent load, requests can fail intermittently with configuration errors or get misconfigured readers.

**Fix:** create the factory per call, or use a `ThreadLocal`.

## 9. `weightContext` and `weightType` are never read

**Where:** `ikey-plus-rest/src/main/java/fr/lis/ikeyplus/rest/RestHandler.java`, line 80.

**Problem:** `initializeConfig` never reads `weightContext` or `weightType` from the query, even though `ConverterRegistrar` registers converters for both and the header prints them. Separately, the `Verbosity` converter rejects uppercase flags, while `VerbosityLevel.fromString` lowercases its input.

**Failure scenario:**
- `?weightContext=CostEffectiveness&weightType=contextual` is silently ignored. Ratings are never applied, and the CONTEXTUAL branch of `bestCharacter` cannot be reached.
- `?verbosity=H` returns 400.

## 10. Verbosity flags are only partly honored

**Where:** `ikey-plus-rest/src/main/java/fr/lis/ikeyplus/rest/RestHandler.java`, line 112.

**Problem:**
- The TXT tree and flat, WIKI flat, DOT and SDD branches never output statistics. The file-based `dumpTxtFile` and `dumpFlatTxtFile` do append `outputTaxonPathStatisticsString`.
- Both WIKI branches drop the header.

**Failure scenario:** `format=txt&verbosity=hs` returns no statistics, and `format=wiki&verbosity=h` returns no header.
