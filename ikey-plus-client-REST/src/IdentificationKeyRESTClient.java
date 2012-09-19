import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * This class is the client of Identification key webservice using REST protocol
 * 
 * @author Florian Causse
 * @created 18-04-2011
 * 
 */
public class IdentificationKeyRESTClient {

	/**
	 * Main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		int i = 0;
		// create n threads
		while (i < 1) {
			i++;
			Thread t = new Thread() {
				public void run() {

					int j = 0;
					// call the webservice n times
					while (j < 1) {
						j++;
						ClientConfig config = new DefaultClientConfig();
						Client client = Client.create(config);
						WebResource service = client.resource("http://localhost:8080/IK_WS_REST-1.0");

						// create params
						MultivaluedMap queryParams = new MultivaluedMapImpl();
						queryParams.add("sddURL",
								"http://www.infosyslab.fr/vibrant/project/test/Cichorieae-fullSDD.xml");
						queryParams.add("format", "interactiveHTML");
						queryParams.add("representation", "flat");
						queryParams.add("fewStatesCharacterFirst", "no");
						queryParams.add("mergeCharacterStatesIfSameDiscrimination", "no");
						queryParams.add("pruning", "no");
						queryParams.add("verbosity", "hs");
						queryParams.add("scoreMethod", "xper");
						queryParams.add("weightContext", "ObservationConvenience");
						queryParams.add("weightType", "global");

						String[] stringArray = new String[1];
						stringArray[0] = MediaType.TEXT_PLAIN;

						// Fluent interfaces
						// System.out.println(service.path("identificationKey").accept(stringArray).post(ClientResponse.class,
						// queryParams));

						// create Identification Key
						System.out.println(service.path("identificationKey").accept(stringArray)
								.post(String.class, queryParams));
					}
				}
			};
			t.start();
		}
	}
}
