
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

    public void issueRootCredential() throws CardException, CredentialsException, CardServiceException {
        IssueCredentialInformation ici = new IssueCredentialInformation("Surfnet", "root");
        IdemixIssueSpecification spec = ici.getIdemixIssueSpecification();
        IdemixPrivateKey isk = ici.getIdemixPrivateKey();
		
        IdemixService is = new IdemixService(getCardService());
        IdemixCredentials ic = new IdemixCredentials(is);
        ic.connect();
        
        is.sendPin(DEFAULT_CRED_PIN);
        Attributes attributes = getSurfnetAttributes();

        ic.issue(spec, isk, attributes, null);
    }

    public void verifyRootCredentialAll() throws CardException, CredentialsException {
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

    public void verifyRootCredentialAll_withDS() throws CardException, CredentialsException, InfoException {

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

    public void verifyRootCredentialNone() throws CardException, CredentialsException {
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

    public void removeRootCredential() throws CardException, CredentialsException, CardServiceException, InfoException {
        CredentialDescription cd = DescriptionStore.getInstance().getCredentialDescriptionByName("Surfnet", "root");

        IdemixService is = getIdemixService();
        IdemixCredentials ic = new IdemixCredentials(is);

        ic.connect();
        is.sendCardPin(DEFAULT_CARD_PIN);
        
        try {
            ic.removeCredential(cd);
        } catch (CardServiceException e) {
            if (!e.getMessage().toUpperCase().contains("6A88")) {
                throw e;
            }
        }
    }

    public void issueStudentCredential() throws CardException, CredentialsException, CardServiceException {
        IssueCredentialInformation ici = new IssueCredentialInformation("RU", "studentCard");
        IdemixIssueSpecification spec = ici.getIdemixIssueSpecification();
        IdemixPrivateKey isk = ici.getIdemixPrivateKey();
		
        IdemixService is = new IdemixService(getCardService());
        IdemixCredentials ic = new IdemixCredentials(is);
        ic.connect();
        
        is.sendPin(DEFAULT_CRED_PIN);
        Attributes attributes = getStudentCardAttributes();

        ic.issue(spec, isk, attributes, null);
    }

    public void verifyStudentCredentialAll() throws CardException, CredentialsException {
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

    private Attributes getSurfnetAttributes() {
        // Return the attributes that have been revealed during the proof
        Attributes attributes = new Attributes();

        attributes.add("userID", "u921154@ru.nl".getBytes());
        attributes.add("securityHash", "DEADBEEF".getBytes());
		
        return attributes;
    }

    public static IdemixService getIdemixService() throws CardException {
            return new IdemixService(getCardService());
    }

    private Attributes getStudentCardAttributes() {
        // Return the attributes that have been revealed during the proof
        Attributes attributes = new Attributes();
        
        System.out.println("Data: " + "Radboud University".getBytes().toString() + " Length: " + "Radboud University".getBytes().length);

        attributes.add("university", "Radboud University".getBytes());
        attributes.add("studentCardNumber", "0812345673".getBytes());
        attributes.add("studentID", "s1234567".getBytes());
        attributes.add("level", "PhD".getBytes());
		
        return attributes;
    }

}

/*

	@Test

	@Test
	public void verifyStudentCredentialNone() throws CardException, CredentialsException {
		VerifyCredentialInformation vci = new VerifyCredentialInformation("RU",
				"studentCard", "RU", "studentCardNone");
		IdemixVerifySpecification vspec = vci.getIdemixVerifySpecification();

		CardService cs = TestSetup.getCardService();
		IdemixCredentials ic = new IdemixCredentials(cs);

		Attributes attr = ic.verify(vspec);
		
		if (attr == null) {
			fail("The proof does not verify");
		} else {
			System.out.println("Proof verified");
		}
		
		attr.print();
	}
	@Test
	public void removeStudentCredential() throws CardException, CredentialsException, CardServiceException, InfoException {
		CredentialDescription cd = DescriptionStore.getInstance().getCredentialDescriptionByName("RU", "studentCard");

		IdemixService is = TestSetup.getIdemixService();
		IdemixCredentials ic = new IdemixCredentials(is);

		ic.connect();
		is.sendCardPin(TestSetup.DEFAULT_CARD_PIN);
		try {
			ic.removeCredential(cd);
		} catch (CardServiceException e) {
			if (!e.getMessage().toUpperCase().contains("6A88")) {
				throw e;
			}
		}
	}

	@Test
	public void issueAgeCredential() throws CardException, CredentialsException, CardServiceException {
		IssueCredentialInformation ici = new IssueCredentialInformation("MijnOverheid", "ageLower");
		IdemixIssueSpecification spec = ici.getIdemixIssueSpecification();
		IdemixPrivateKey isk = ici.getIdemixPrivateKey();
		
		IdemixService is = new IdemixService(TestSetup.getCardService());
		IdemixCredentials ic = new IdemixCredentials(is);
		ic.connect();
		is.sendPin(TestSetup.DEFAULT_CRED_PIN);
		Attributes attributes = getAgeAttributes();
		ic.issue(spec, isk, attributes, null);
	}


	@Test
	public void verifyAgeCredentialAll() throws CardException, CredentialsException {
		VerifyCredentialInformation vci = new VerifyCredentialInformation("MijnOverheid",
				"ageLower", "MijnOverheid", "ageLowerAll");
		IdemixVerifySpecification vspec = vci.getIdemixVerifySpecification();

		CardService cs = TestSetup.getCardService();
		IdemixCredentials ic = new IdemixCredentials(cs);

		Attributes attr = ic.verify(vspec);
		
		if (attr == null) {
			fail("The proof does not verify");
		} else {
			System.out.println("Proof verified");
		}
		
		attr.print();
	}

	@Test
	public void verifyAgeCredentialNone() throws CardException, CredentialsException {
		VerifyCredentialInformation vci = new VerifyCredentialInformation("MijnOverheid",
				"ageLower", "MijnOverheid", "ageLowerNone");
		IdemixVerifySpecification vspec = vci.getIdemixVerifySpecification();

		CardService cs = TestSetup.getCardService();
		IdemixCredentials ic = new IdemixCredentials(cs);

		Attributes attr = ic.verify(vspec);
		
		if (attr == null) {
			fail("The proof does not verify");
		} else {
			System.out.println("Proof verified");
		}
		
		attr.print();
	}
	
	@Test
	public void verifyAgeCredentialOver16() throws CardException, CredentialsException {
		VerifyCredentialInformation vci = new VerifyCredentialInformation("MijnOverheid",
				"ageLower", "UitzendingGemist", "ageLowerOver16");
		IdemixVerifySpecification vspec = vci.getIdemixVerifySpecification();

		CardService cs = TestSetup.getCardService();
		IdemixCredentials ic = new IdemixCredentials(cs);

		Attributes attr = ic.verify(vspec);

		if (attr == null) {
			fail("The proof does not verify");
		} else {
			System.out.println("Proof verified");
		}

		attr.print();
	}

	@Test
	public void removeAgeCredential() throws CardException, CredentialsException, CardServiceException, InfoException {
		CredentialDescription cd = DescriptionStore.getInstance().getCredentialDescriptionByName("MijnOverheid", "ageLower");

		IdemixService is = TestSetup.getIdemixService();
		IdemixCredentials ic = new IdemixCredentials(is);

		ic.connect();
		is.sendCardPin(TestSetup.DEFAULT_CARD_PIN);
		try {
			ic.removeCredential(cd);
		} catch (CardServiceException e) {
			if (!e.getMessage().toUpperCase().contains("6A88")) {
				throw e;
			}
		}
	}

	@Test
	public void issueAddressNijmegenCredential() throws CardException, CredentialsException, CardServiceException {
		IssueCredentialInformation ici = new IssueCredentialInformation("MijnOverheid", "address");
		IdemixIssueSpecification spec = ici.getIdemixIssueSpecification();
		IdemixPrivateKey isk = ici.getIdemixPrivateKey();
		
		IdemixService is = new IdemixService(TestSetup.getCardService());
		IdemixCredentials ic = new IdemixCredentials(is);
		ic.connect();
		is.sendPin(TestSetup.DEFAULT_CRED_PIN);
		Attributes attributes = getAddressNijmegenAttributes();
		ic.issue(spec, isk, attributes, null);
	}
	
	@Test
	public void removeAddressNijmegenCredential() throws CardException, CredentialsException, CardServiceException, InfoException {
		CredentialDescription cd = DescriptionStore.getInstance().getCredentialDescriptionByName("MijnOverheid", "address");

		IdemixService is = TestSetup.getIdemixService();
		IdemixCredentials ic = new IdemixCredentials(is);

		ic.connect();
		is.sendCardPin(TestSetup.DEFAULT_CARD_PIN);
		try {
			ic.removeCredential(cd);
		} catch (CardServiceException e) {
			if (!e.getMessage().toUpperCase().contains("6A88")) {
				throw e;
			}
		}
	}

	@Test
	public void issueAddressReuverCredential() throws CardException, CredentialsException, CardServiceException {
		IssueCredentialInformation ici = new IssueCredentialInformation("MijnOverheid", "address");
		IdemixIssueSpecification spec = ici.getIdemixIssueSpecification();
		IdemixPrivateKey isk = ici.getIdemixPrivateKey();
		
		IdemixService is = new IdemixService(TestSetup.getCardService());
		IdemixCredentials ic = new IdemixCredentials(is);
		ic.connect();
		is.sendPin(TestSetup.DEFAULT_CRED_PIN);
		Attributes attributes = getAddressReuverAttributes();
		spec.setCardVersion(is.getCardVersion());
		ic.issue(spec, isk, attributes, null);
	}
	
	@Test
	public void verifyAddressCredentialAll() throws CardException, CredentialsException {
		VerifyCredentialInformation vci = new VerifyCredentialInformation("MijnOverheid",
				"address", "MijnOverheid", "addressAll");
		IdemixVerifySpecification vspec = vci.getIdemixVerifySpecification();

		CardService cs = TestSetup.getCardService();
		IdemixCredentials ic = new IdemixCredentials(cs);

		Attributes attr = ic.verify(vspec);
		
		if (attr == null) {
			fail("The proof does not verify");
		} else {
			System.out.println("Proof verified");
		}
		
		attr.print();
	}

	@Test
	public void verifyAddressCredentialNone() throws CardException, CredentialsException {
		VerifyCredentialInformation vci = new VerifyCredentialInformation("MijnOverheid",
				"address", "MijnOverheid", "addressNone");
		IdemixVerifySpecification vspec = vci.getIdemixVerifySpecification();

		CardService cs = TestSetup.getCardService();
		IdemixCredentials ic = new IdemixCredentials(cs);

		Attributes attr = ic.verify(vspec);
		
		if (attr == null) {
			fail("The proof does not verify");
		} else {
			System.out.println("Proof verified");
		}
		
		attr.print();
	}
	
	@Test
	public void removeAddressCredential() throws CardException, CredentialsException, CardServiceException, InfoException {
		CredentialDescription cd = DescriptionStore.getInstance().getCredentialDescriptionByName("MijnOverheid", "address");

		IdemixService is = TestSetup.getIdemixService();
		IdemixCredentials ic = new IdemixCredentials(is);

		ic.connect();
		is.sendCardPin(TestSetup.DEFAULT_CARD_PIN);
		try {
			ic.removeCredential(cd);
		} catch (CardServiceException e) {
			if (!e.getMessage().toUpperCase().contains("6A88")) {
				throw e;
			}
		}
	}

	@Test
	public void issueMijnOverheidRoot() throws CardException,
			CredentialsException, CardServiceException {
		IssueCredentialInformation ici = new IssueCredentialInformation(
				"MijnOverheid", "root");

		Attributes attributes = new Attributes();
		attributes.add("BSN", "123456789".getBytes());

		issue(ici, attributes);
	}

	@Test
	public void verifyMijnOverheidRoot() throws CardException,
			CredentialsException, CardServiceException, InfoException {
		VerifyCredentialInformation vci = new VerifyCredentialInformation(
				"MijnOverheid", "rootAll");
		verify(vci);
	}

	@Test
	public void removeMijnOverheidRoot() throws CardException,
			CredentialsException, CardServiceException, InfoException {
		CredentialDescription cd = DescriptionStore.getInstance()
				.getCredentialDescriptionByName("MijnOverheid", "root");
		remove(cd);
	}

	@Test
	public void issueFullNameCredential() throws CardException, CredentialsException,
			CardServiceException {
		IssueCredentialInformation ici = new IssueCredentialInformation(
				"MijnOverheid", "fullName");

		Attributes attributes = new Attributes();
		attributes.add("firstnames", "Johan Pieter".getBytes());
		attributes.add("firstname", "Johan".getBytes());
		attributes.add("familyname", "Stuivezand".getBytes());
		attributes.add("prefix", "van".getBytes());

		issue(ici, attributes);
	}

	@Test
	public void verifyFullNameCredential() throws CardException,
			CredentialsException, CardServiceException, InfoException {
		VerifyCredentialInformation vci = new VerifyCredentialInformation(
				"MijnOverheid", "fullNameAll");
		verify(vci);
	}

	@Test
	public void removeFullNameCredential() throws CardException,
			CredentialsException, CardServiceException, InfoException {
		CredentialDescription cd = DescriptionStore.getInstance()
				.getCredentialDescriptionByName("MijnOverheid", "fullName");
		remove(cd);
	}

	@Test
	public void issueBirthCertificate() throws CardException, CredentialsException,
			CardServiceException {
		IssueCredentialInformation ici = new IssueCredentialInformation(
				"MijnOverheid", "birthCertificate");

		Attributes attributes = new Attributes();
		attributes.add("dateofbirth", "29-2-2004".getBytes());
		attributes.add("placeofbirth", "Stuivezand".getBytes());
		attributes.add("countryofbirth", "Nederland".getBytes());
		attributes.add("gender", "male".getBytes());

		issue(ici, attributes);
	}

	@Test
	public void verifyBirthCertificate() throws CardException,
			CredentialsException, CardServiceException, InfoException {
		VerifyCredentialInformation vci = new VerifyCredentialInformation(
				"MijnOverheid", "birthCertificateAll");
		verify(vci);
	}

	@Test
	public void removeBirthCertificate() throws CardException,
			CredentialsException, CardServiceException, InfoException {
		CredentialDescription cd = DescriptionStore.getInstance()
				.getCredentialDescriptionByName("MijnOverheid", "birthCertificate");
		remove(cd);
	}

	@Test
	public void issueSeniorAgeCredential() throws CardException, CredentialsException,
			CardServiceException {
		IssueCredentialInformation ici = new IssueCredentialInformation(
				"MijnOverheid", "ageHigher");

		Attributes attributes = new Attributes();
		attributes.add("over50", "yes".getBytes());
		attributes.add("over60", "no".getBytes());
		attributes.add("over65", "no".getBytes());
		attributes.add("over75", "no".getBytes());

		issue(ici, attributes);
	}

	@Test
	public void verifySeniorAgeCredential() throws CardException,
			CredentialsException, CardServiceException, InfoException {
		VerifyCredentialInformation vci = new VerifyCredentialInformation(
				"MijnOverheid", "ageHigherAll");
		verify(vci);
	}

	@Test
	public void removeSeniorAgeCredential() throws CardException,
			CredentialsException, CardServiceException, InfoException {
		CredentialDescription cd = DescriptionStore.getInstance()
				.getCredentialDescriptionByName("MijnOverheid", "ageHigher");
		remove(cd);
	}

	@Test
	public void issueIRMATubeMemberCredential() throws CardException, CredentialsException,
			CardServiceException {
		IssueCredentialInformation ici = new IssueCredentialInformation(
				"IRMATube", "member");

		Attributes attributes = new Attributes();
		attributes.add("name", "J.P. Stuivezand".getBytes());
		attributes.add("type", "regular".getBytes());
		attributes.add("id", "123456".getBytes());

		issue(ici, attributes);
	}

	@Test
	public void verifyIRMATubeMemberCredential() throws CardException,
			CredentialsException, CardServiceException, InfoException {
		VerifyCredentialInformation vci = new VerifyCredentialInformation(
				"IRMATube", "memberAll");
		verify(vci);
	}

	@Test
	public void verifyIRMATubeMemberTypeCredential() throws CardException,
			CredentialsException, CardServiceException, InfoException {
		VerifyCredentialInformation vci = new VerifyCredentialInformation(
				"IRMATube", "memberType");
		verify(vci);
	}

	@Test
	public void removeIRMATubeMemberCredential() throws CardException,
			CredentialsException, CardServiceException, InfoException {
		CredentialDescription cd = DescriptionStore.getInstance()
				.getCredentialDescriptionByName("IRMATube", "member");
		remove(cd);
	}

	@Test
	public void issueIRMAWikiMemberCredential() throws CardException, CredentialsException,
			CardServiceException {
		IssueCredentialInformation ici = new IssueCredentialInformation(
				"IRMAWiki", "member");

		Attributes attributes = new Attributes();
		attributes.add("nickname", "Stuifje".getBytes());
		attributes.add("type", "regular".getBytes());

		issue(ici, attributes);
	}

	@Test
	public void verifyIRMAWikiMemberCredential() throws CardException,
			CredentialsException, CardServiceException, InfoException {
		VerifyCredentialInformation vci = new VerifyCredentialInformation(
				"IRMAWiki", "memberAll");
		verify(vci);
	}

	@Test
	public void removeIRMAWikiMemberCredential() throws CardException,
			CredentialsException, CardServiceException, InfoException {
		CredentialDescription cd = DescriptionStore.getInstance()
				.getCredentialDescriptionByName("IRMAWiki", "member");
		remove(cd);
	}

	@Test
	public void verifyIRMAWikiSurfnetRootNone() throws CardException,
			CredentialsException, CardServiceException, InfoException {
		VerifyCredentialInformation vci = new VerifyCredentialInformation(
				"IRMAWiki", "surfnetRootNone");
		verify(vci);
	}

	private void issue(IssueCredentialInformation ici, Attributes attributes)
			throws CardException, CredentialsException, CardServiceException {
		IdemixIssueSpecification spec = ici.getIdemixIssueSpecification();
		IdemixPrivateKey isk = ici.getIdemixPrivateKey();

		IdemixService is = new IdemixService(TestSetup.getCardService());
		IdemixCredentials ic = new IdemixCredentials(is);
		ic.connect();
		is.sendPin(TestSetup.DEFAULT_CRED_PIN);
		ic.issue(spec, isk, attributes, null);
	}

	private void verify(VerifyCredentialInformation vci) throws CardException, CredentialsException {
		IdemixVerifySpecification vspec = vci.getIdemixVerifySpecification();

		CardService cs = TestSetup.getCardService();
		IdemixCredentials ic = new IdemixCredentials(cs);

		Attributes attr = ic.verify(vspec);

		if (attr == null) {
			fail("The proof does not verify");
		} else {
			System.out.println("Proof verified");
		}

		attr.print();
	}

	private void remove(CredentialDescription cd) throws CardException, CredentialsException, CardServiceException, InfoException {
		IdemixService is = TestSetup.getIdemixService();
		IdemixCredentials ic = new IdemixCredentials(is);

		ic.connect();
		is.sendCardPin(TestSetup.DEFAULT_CARD_PIN);
		try {
			ic.removeCredential(cd);
		} catch (CardServiceException e) {
			if (!e.getMessage().toUpperCase().contains("6A88")) {
				throw e;
			}
		}
	}


    
    private Attributes getAgeAttributes () {
        Attributes attributes = new Attributes();

		attributes.add("over12", "yes".getBytes());
		attributes.add("over16", "yes".getBytes());
		attributes.add("over18", "yes".getBytes());
		attributes.add("over21", "yes".getBytes());
		
		return attributes;
    }
    
    private Attributes getAddressNijmegenAttributes () {
        Attributes attributes = new Attributes();

		attributes.add("country", "Nederland".getBytes());
		attributes.add("city", "Nijmegen".getBytes());
		attributes.add("street", "Heyendaalseweg 135".getBytes());
		attributes.add("zipcode", "6525 AJ".getBytes());
		
		return attributes;
    }

    private Attributes getAddressReuverAttributes () {
        Attributes attributes = new Attributes();

		attributes.add("country", "Nederland".getBytes());
		attributes.add("city", "Reuver".getBytes());
		attributes.add("street", "Snavelbies 19".getBytes());
		attributes.add("zipcode", "5953 MR".getBytes());
		
		return attributes;
    }
}
*/