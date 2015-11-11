package fr.lis.ikeyplus.rest;

import fr.lis.ikeyplus.IO.SDDSaxParser;
import fr.lis.ikeyplus.services.IdentificationKeyGeneratorImpl;
import fr.lis.ikeyplus.utils.IkeyException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class IdentificationKeyResourceTest {

    public static final String INVALID_SDD_URL = "https://www.dropbox.com/s/umglan4vsvpg96s/doc_xper_2.3_core_en.tex?dl=1";
    @Rule
    public ExpectedException thrown = ExpectedException.none();


    public static final String WEIGHT_TYPE = "global";
    public static final String WEIGHT_CONTEXT = "";
    public static final String SCORE_METHOD = "xper";
    public static final String VERBOSITY = "";
    public static final String REPRESENTATION = "flat";
    public static final String FORMAT = "txt";
    public static final boolean FEW_STATES_CHARACTER_FIRST = false;
    public static final boolean MERGE_CHARACTER_STATES_IF_SAME_DISCRIMINATION = false;
    public static final boolean PRUNING = false;
    public static final String VALID_SDD_URL = "https://www.dropbox.com/s/lzdkhszlft00u9e/genetta.sdd.xml?dl=1";
    private IdentificationKeyResource endpoint;

    @Test
    public void should_throw_exception_for_invalid_url() throws Exception {
        thrown.expect(IkeyException.class);
        thrown.expectMessage("The URL to SDD file is not correct");
        //thrown.expectCause(equalTo(new MalformedURLException("")));

        endpoint.createIdentificationKey(
                "",
                FORMAT,
                REPRESENTATION,
                FEW_STATES_CHARACTER_FIRST,
                MERGE_CHARACTER_STATES_IF_SAME_DISCRIMINATION,
                PRUNING,
                VERBOSITY,
                SCORE_METHOD,
                WEIGHT_CONTEXT,
                WEIGHT_TYPE);
    }

    @Test
    public void should_throw_exception_for_invalid_sdd_file() throws Exception {
        thrown.expect(IkeyException.class);
        thrown.expectMessage("An error occurred during the SDD parsing");

        endpoint.createIdentificationKey(
                INVALID_SDD_URL,
                FORMAT,
                REPRESENTATION,
                FEW_STATES_CHARACTER_FIRST,
                MERGE_CHARACTER_STATES_IF_SAME_DISCRIMINATION,
                PRUNING,
                VERBOSITY,
                SCORE_METHOD,
                WEIGHT_CONTEXT,
                WEIGHT_TYPE);
    }


    @Before
    public void setUp() {
        endpoint = new IdentificationKeyResource(new SDDSaxParser(), new IdentificationKeyGeneratorImpl());
    }

}