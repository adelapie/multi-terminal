package org.irma;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;

import net.sourceforge.scuba.smartcards.CardService;
import net.sourceforge.scuba.smartcards.CardServiceException;
import net.sourceforge.scuba.smartcards.TerminalCardService;

import org.irmacard.credentials.Attributes;
import org.irmacard.credentials.CredentialsException;
import org.irmacard.credentials.idemix.IdemixCredentials;
import org.irmacard.credentials.idemix.IdemixPrivateKey;
import org.irmacard.credentials.idemix.spec.IdemixIssueSpecification;
import org.irmacard.credentials.idemix.spec.IdemixVerifySpecification;
import org.irmacard.credentials.idemix.util.CredentialInformation;
import org.irmacard.credentials.idemix.util.IssueCredentialInformation;
import org.irmacard.credentials.idemix.util.VerifyCredentialInformation;
import org.irmacard.credentials.info.CredentialDescription;
import org.irmacard.credentials.info.DescriptionStore;
import org.irmacard.credentials.info.InfoException;
import org.irmacard.idemix.IdemixService;

import com.ibm.zurich.credsystem.utils.Locations;
import com.ibm.zurich.idmx.issuance.IssuanceSpec;
import com.ibm.zurich.idmx.key.IssuerKeyPair;
import com.ibm.zurich.idmx.key.IssuerPrivateKey;
import com.ibm.zurich.idmx.showproof.ProofSpec;
import com.ibm.zurich.idmx.utils.StructureStore;

/**
 * Unit test for simple Client.
 */
public class IRMAClientTest 
    extends TestCase
{

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public IRMAClientTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( IRMAClientTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testClient() throws CardException, CardServiceException, InfoException, CredentialsException {
        IRMAClient app = new IRMAClient();
        
//        System.out.println("Read info");
//        app.readInfo();

//        System.out.println("Initialize Information");
//        app.initializeInformation();

//        System.out.println("################################ Generate master secret");
//        app.generateMasterSecret();     

//        System.out.println("################################ Issue root credential");
 //       app.issueRootCredential();

//        System.out.println("################################ Verify root credential");
//        app.verifyRootCredentialAll();

//        System.out.println("################################ Verify root credential with DS");
//        app.verifyRootCredentialAll_withDS();

//        System.out.println("################################ Verify root credential none");
//        app.verifyRootCredentialNone();

//       System.out.println("################################ Issue student credential");
//        app.issueStudentCredential();

//          System.out.println("################################ Verify student credential");
//          app.verifyStudentCredentialAll();

//        System.out.println("################################ Remove root credential");
//        app.removeRootCredential();

       // System.out.println("################################ Verify root credential #2");
       // app.verifyRootCredentialAll();

//        System.out.println("################################ Retrieve log");
//        app.getLog();
    }
}
