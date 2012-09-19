import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * This class is the client of Identification key webservice using SOAP protocol
 * 
 * @author Florian Causse
 * @created 06-04-2011
 * 
 */
public class IdentificationKeySOAPClient {

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
						ApplicationContext context = new FileSystemXmlApplicationContext(
								"application-context.xml");
						JaxWsProxyFactoryBean fb = (JaxWsProxyFactoryBean) context
								.getBean("jaxWsProxyFactoryBean");
						IIdentificationKey service = (IIdentificationKey) fb.create();
						System.out.println(service.createIdentificationKey(
								"http://www.infosyslab.fr/vibrant/project/test/Cichorieae-fullSDD.xml",
								"html", "tree", "no", "no", "no", "hs", "xper", "ObservationConvenience",
								"global"));
					}
				}
			};
			t.start();
		}

	}
}
