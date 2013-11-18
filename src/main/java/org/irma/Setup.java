
package org.irma;

import java.io.File;
import java.net.URI;
import java.math.BigInteger;
import java.net.URISyntaxException;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;

import net.sourceforge.scuba.smartcards.CardService;
import net.sourceforge.scuba.smartcards.TerminalCardService;
import net.sourceforge.scuba.smartcards.CardServiceException;

import com.ibm.zurich.credsystem.utils.Locations;
import com.ibm.zurich.idmx.issuance.IssuanceSpec;
import com.ibm.zurich.idmx.key.IssuerKeyPair;
import com.ibm.zurich.idmx.key.IssuerPrivateKey;
import com.ibm.zurich.idmx.showproof.ProofSpec;
import com.ibm.zurich.idmx.utils.StructureStore;

import org.irmacard.credentials.Attributes;
import org.irmacard.credentials.CredentialsException;
import org.irmacard.credentials.idemix.IdemixCredentials;
import org.irmacard.credentials.idemix.IdemixPrivateKey;
import org.irmacard.credentials.idemix.spec.IdemixIssueSpecification;
import org.irmacard.credentials.idemix.spec.IdemixVerifySpecification;
import org.irmacard.credentials.idemix.util.CredentialInformation;
import org.irmacard.credentials.idemix.util.IssueCredentialInformation;
import org.irmacard.credentials.idemix.util.VerifyCredentialInformation;
import org.irmacard.idemix.util.IdemixLogEntry;
import org.irmacard.credentials.info.CredentialDescription;
import org.irmacard.credentials.info.DescriptionStore;
import org.irmacard.credentials.info.InfoException;
import org.irmacard.idemix.IdemixService;

import java.util.Date;
import java.util.List;

public class Setup 
{

    /** Actual location of the files. */
	/** TODO: keep this in mind, do we need BASE_LOCATION to point to .../parameter/
	 *  to keep idemix-library happy, i.e. so that it can find gp.xml and sp.xml?
	 */
    public static final URI BASE_LOCATION = new File(
            System.getProperty("user.dir")).toURI().resolve("irma_configuration/RU/");
    
    /** Actual location of the public issuer-related files. */
    public static final URI ISSUER_LOCATION = BASE_LOCATION;
	
    /** URIs and locations for issuer */
    public static final URI ISSUER_SK_LOCATION = ISSUER_LOCATION.resolve("private/isk.xml");
    public static final URI ISSUER_PK_LOCATION = ISSUER_LOCATION.resolve("ipk.xml");
    
    /** Credential location */
    public static final String CRED_STRUCT_NAME = "studentCard";
    public static final URI CRED_STRUCT_LOCATION = BASE_LOCATION
            .resolve("Issues/studentCard/structure.xml");
    
    /** Proof specification location */
    public static final URI PROOF_SPEC_LOCATION = BASE_LOCATION
                            .resolve("Verifies/studentCardAll/specification.xml");
    
    /** Ids used within the test files to identify the elements. */
    public static URI BASE_ID = null;
    public static URI ISSUER_ID = null;
    public static URI CRED_STRUCT_ID = null;
    static {
        try {
            BASE_ID = new URI("http://www.irmacard.org/credentials/phase1/RU/");
            ISSUER_ID = new URI("http://www.irmacard.org/credentials/phase1/RU/");
            CRED_STRUCT_ID = new URI("http://www.irmacard.org/credentials/phase1/RU/" + CRED_STRUCT_NAME + "/structure.xml");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    
    /** The identifier of the credential on the smartcard */
    public static short CRED_NR = (short) 4;

    /** Attribute values */
    public static final BigInteger ATTRIBUTE_VALUE_1 = BigInteger.valueOf(1313);
    public static final BigInteger ATTRIBUTE_VALUE_2 = BigInteger.valueOf(1314);
    public static final BigInteger ATTRIBUTE_VALUE_3 = BigInteger.valueOf(1315);
    public static final BigInteger ATTRIBUTE_VALUE_4 = BigInteger.valueOf(1316);
    public static final BigInteger ATTRIBUTE_VALUE_5 = BigInteger.valueOf(1317);

    /**
     * Default PIN of card.
     */
    public static final byte[] DEFAULT_CRED_PIN = {0x30, 0x30, 0x30, 0x30};
    public static final byte[] DEFAULT_CARD_PIN = {0x30, 0x30, 0x30, 0x30, 0x30, 0x30};


    public static void readInfo()
    {        
        try {
            CardTerminal terminal = TerminalFactory.getDefault().terminals().list().get(0);            
            IdemixService is = new IdemixService(new TerminalCardService(terminal));
        
            is.open();
        } catch (Exception e) {
        
        }
    }

    public static void initializeInformation() throws InfoException {
        URI core = new File(System
                       .getProperty("user.dir")).toURI()
                       .resolve("irma_configuration/");
		
        CredentialInformation.setCoreLocation(core);
        DescriptionStore.setCoreLocation(core);
        DescriptionStore.getInstance();
    }

    public void generateMasterSecret() throws CardException, CardServiceException {
        IdemixService is = new IdemixService(getCardService());
        is.open();
        
        try {
            is.generateMasterSecret();
        } catch (CardServiceException e) {
            if (!e.getMessage().contains("6986")) {
                throw e;
            }
        }

    }

    public static void issueRootCredential(byte[] pin) throws CardException, CredentialsException, CardServiceException, InfoException {
        
        initializeInformation();

        IssueCredentialInformation ici = new IssueCredentialInformation("Surfnet", "root");
        IdemixIssueSpecification spec = ici.getIdemixIssueSpecification();
        IdemixPrivateKey isk = ici.getIdemixPrivateKey();
		
        IdemixService is = new IdemixService(getCardService());
        IdemixCredentials ic = new IdemixCredentials(is);
        ic.connect();
        
        is.sendPin(pin);
        Attributes attributes = getSurfnetAttributes();

        ic.issue(spec, isk, attributes, null);
    }

    public static void verifyRootCredentialAll() throws CardException, CredentialsException, InfoException {

        initializeInformation();        
        
        VerifyCredentialInformation vci = new VerifyCredentialInformation(
				"Surfnet", "root", "Surfnet", "rootAll");
        
        IdemixVerifySpecification vspec = vci.getIdemixVerifySpecification();

        CardService cs = getCardService();
        IdemixCredentials ic = new IdemixCredentials(cs);

        Attributes attr = ic.verify(vspec);
		
        if (attr == null) {
            System.out.println("The proof does not verify");
        } else {
            System.out.println("Proof verified");
        }
		
        attr.print();
    }

    public static void verifyRootCredentialAll_withDS() throws CardException, CredentialsException, InfoException {

        initializeInformation();        

        VerifyCredentialInformation vci = new VerifyCredentialInformation("Surfnet", "rootAll");
        IdemixVerifySpecification vspec = vci.getIdemixVerifySpecification();

        CardService cs = getCardService();
        IdemixCredentials ic = new IdemixCredentials(cs);

        Attributes attr = ic.verify(vspec);

        if (attr == null) {
            System.out.println("The proof does not verify");
        } else {
            System.out.println("Proof verified");
        }

        attr.print();
    }

    public static void verifyRootCredentialNone() throws CardException, CredentialsException, InfoException {
        initializeInformation();        

        VerifyCredentialInformation vci = new VerifyCredentialInformation(
            "Surfnet", "root", "Surfnet", "rootNone");
        
        IdemixVerifySpecification vspec = vci.getIdemixVerifySpecification();

        CardService cs = getCardService();
        IdemixCredentials ic = new IdemixCredentials(cs);

        Attributes attr = ic.verify(vspec);
		
        if (attr == null) {
            System.out.println("The proof does not verify");
        } else {
            System.out.println("Proof verified");
        }
		
        attr.print();
    }

    public static void removeRootCredential(byte[] pin) throws CardException, CredentialsException, CardServiceException, InfoException {
        initializeInformation();

        CredentialDescription cd = DescriptionStore.getInstance().getCredentialDescriptionByName("Surfnet", "root");

        IdemixService is = getIdemixService();
        IdemixCredentials ic = new IdemixCredentials(is);

        ic.connect();
        is.sendCardPin(pin);
        
        try {
            ic.removeCredential(cd);
        } catch (CardServiceException e) {
            if (!e.getMessage().toUpperCase().contains("6A88")) {
                throw e;
            }
        }
    }

    public static void issueStudentCredential(byte[] pin) throws CardException, CredentialsException, CardServiceException, InfoException {
        initializeInformation();

        IssueCredentialInformation ici = new IssueCredentialInformation("RU", "studentCard");
        IdemixIssueSpecification spec = ici.getIdemixIssueSpecification();
        IdemixPrivateKey isk = ici.getIdemixPrivateKey();
		
        IdemixService is = new IdemixService(getCardService());
        IdemixCredentials ic = new IdemixCredentials(is);
        ic.connect();
        
        is.sendPin(pin);
        Attributes attributes = getStudentCardAttributes();

        ic.issue(spec, isk, attributes, null);
    }

    public static void verifyStudentCredentialAll() throws CardException, CredentialsException, InfoException {
        initializeInformation();
        VerifyCredentialInformation vci = new VerifyCredentialInformation("RU", "studentCard", "RU", "studentCardAll");
        
        IdemixVerifySpecification vspec = vci.getIdemixVerifySpecification();
        IdemixCredentials ic = new IdemixCredentials(getCardService());

        Attributes attr = ic.verify(vspec);
		
        if (attr == null) {
            System.out.println("The proof does not verify");
        } else {
            System.out.println("Proof verified");
        }
		
        attr.print();
    }

    public static void getLog(byte[] pin) throws CardException, CardServiceException, CredentialsException {
        IdemixService is = new IdemixService(getCardService());
        IdemixCredentials ic = new IdemixCredentials(is);
        ic.connect();
        
        is.sendCardPin(pin);
        
        List<IdemixLogEntry> list = is.getLogEntries();
       
        System.out.println("IRMA Log, " + list.size() + " elements.");
       
        for(IdemixLogEntry l : list) {
            System.out.println("\n[*] Entry\n");
            l.print();
        }
    }
    
    private static CardService getCardService() throws CardException {
		CardTerminal terminal = TerminalFactory.getDefault().terminals().list().get(0);
    	return new TerminalCardService(terminal);
    }

    private static Attributes getSurfnetAttributes() {
        // Return the attributes that have been revealed during the proof
        Attributes attributes = new Attributes();

        attributes.add("userID", "u921154@ru.nl".getBytes());
        attributes.add("securityHash", "DEADBEEF".getBytes());
		
        return attributes;
    }

    public static IdemixService getIdemixService() throws CardException {
            return new IdemixService(getCardService());
    }

    private static Attributes getStudentCardAttributes() {
        // Return the attributes that have been revealed during the proof
        Attributes attributes = new Attributes();
        
        System.out.println("Data: " + "Radboud University".getBytes().toString() + " Length: " + "Radboud University".getBytes().length);

        attributes.add("university", "Radboud University".getBytes());
        attributes.add("studentCardNumber", "0812345673".getBytes());
        attributes.add("studentID", "s1234567".getBytes());
        attributes.add("level", "PhD".getBytes());
		
        return attributes;
    }
    
    public static void removeStudentCredential(byte[] pin) throws CardException, CredentialsException, CardServiceException, InfoException {
                initializeInformation();
		CredentialDescription cd = DescriptionStore.getInstance().getCredentialDescriptionByName("RU", "studentCard");

		IdemixService is = getIdemixService();
		IdemixCredentials ic = new IdemixCredentials(is);

		ic.connect();
		is.sendCardPin(pin);
		try {
			ic.removeCredential(cd);
		} catch (CardServiceException e) {
			if (!e.getMessage().toUpperCase().contains("6A88")) {
				throw e;
			}
		}
	}

	public static void verifyStudentCredentialNone() throws CardException, CredentialsException, InfoException {
		initializeInformation();
		VerifyCredentialInformation vci = new VerifyCredentialInformation("RU",
				"studentCard", "RU", "studentCardNone");
		IdemixVerifySpecification vspec = vci.getIdemixVerifySpecification();

		CardService cs = getCardService();
		IdemixCredentials ic = new IdemixCredentials(cs);

		Attributes attr = ic.verify(vspec);
		
		if (attr == null) {
			System.out.println("The proof does not verify");
		} else {
			System.out.println("Proof verified");
		}
		
		attr.print();
	}
}

