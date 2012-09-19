import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

/**
 * Identification key webservice Interface
 * 
 * @author Florian Causse
 * @created 06-04-2011
 */
@WebService
public interface IIdentificationKey {

	@WebResult(name = "identificationKeyResponse")
	String createIdentificationKey(
			@WebParam(name = "sddURL") String sddURL,
			@WebParam(name = "format") String format,
			@WebParam(name = "representation") String representation,
			@WebParam(name = "fewStatesCharacterFirst") String fewStatesCharacterFirst,
			@WebParam(name = "mergeCharacterStatesIfSameDiscrimination") String mergeCharacterStatesIfSameDiscrimination,
			@WebParam(name = "pruning") String pruning, @WebParam(name = "verbosity") String verbosity,
			@WebParam(name = "scoreMethod") String scoreMethod,
			@WebParam(name = "weightContext") String weightContext,
			@WebParam(name = "weightType") String weightType);
}