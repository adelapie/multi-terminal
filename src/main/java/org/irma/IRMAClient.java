
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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.PosixParser;

import org.apache.commons.codec.binary.Hex;

import java.util.Date;
import java.util.List;

import org.irmacard.idemix.util.CardVersion;

public class IRMAClient 
{
   private static Options createOptions() {
    Options options = new Options();
    options.addOption("h", "help", false, "print this message and exit");
    options.addOption("i", "info-card", false, "get information about IRMA card");
    options.addOption("vcp", "verify-cred-pin", true, "verify cred pin status (4-digit)");
    options.addOption("vap", "verify-card-pin", true, "verify admin pin status (6-digit)");
    options.addOption("l", "log", true, "get log entries - requires admin pin");
    options.addOption("ir", "issue-root-cred", true, "issue root cred - requires cred pin");
    options.addOption("vr", "verify-root-cred", false, "verify root cred - all");
    options.addOption("vrds", "verify-root-cred-ds", false, "verify root cred - all with DS");
    options.addOption("vrn", "verity-root-cred-none", false, "verify root cred - none");
    options.addOption("vsn", "verity-student-cred-none", false, "verify student cred - none");
    options.addOption("rr", "remove-root-cred", true, "remove root cred - requires admin pin");
    options.addOption("is", "issue-student-cred", true, "issue student cred - requires cred pin");
    options.addOption("vs", "verify-student-cred", false, "verify student credential - all");
    options.addOption("rs", "remove-student-cred", true, "remove student cred - requires admin pin");
    options.addOption("qc", "query-cred-pin", false, "query credential pin");
    options.addOption("qa", "query-admin-pin", false, "query admin pin");

    OptionBuilder.withArgName("old-pin new-pin");
    OptionBuilder.hasArgs(2);
    OptionBuilder.withDescription("update admin pin (6-digit)");

    Option updateCardPin = OptionBuilder.create("uap");
    options.addOption(updateCardPin);

    OptionBuilder.withArgName("admin-pin new-cred-pin");
    OptionBuilder.hasArgs(2);
    OptionBuilder.withDescription("update cred pin (4-digit)");

    Option updateCredentialPin = OptionBuilder.create("ucp");
    options.addOption(updateCredentialPin);

    return options;
  }

  private static void showHelp(Options options) {
    HelpFormatter h = new HelpFormatter();
    h.printHelp("IRMA Terminal", options);
    System.exit(-1);
  }

  public static void main(String[] args) {
  
    Options options = createOptions();
    try {
      CommandLineParser parser = new PosixParser();
      CommandLine cmd = parser.parse(options, args);
      
      if(cmd.hasOption("h")) {
        showHelp(options);
      } else if (cmd.hasOption("vcp")) {
        try {
          String pin = cmd.getOptionValue("vcp");
          byte[] hexPin = pin.getBytes("UTF-8");
                    
          CardTerminal terminal = TerminalFactory.getDefault().terminals().list().get(0);            
          IdemixService is = new IdemixService(new TerminalCardService(terminal));
          
          is.open();
          is.sendPin(hexPin);
        } catch (Exception e) {
          System.out.println(e);
        
        }
      } else if (cmd.hasOption("vap")) {
        try {
          String pin = cmd.getOptionValue("vap");
          byte[] hexPin = pin.getBytes("UTF-8");
                    
          CardTerminal terminal = TerminalFactory.getDefault().terminals().list().get(0);            
          IdemixService is = new IdemixService(new TerminalCardService(terminal));
          
          is.open();
          is.sendCardPin(hexPin);
        } catch (Exception e) {
          System.out.println(e);
        
        }
      } else if (cmd.hasOption("l")) {
        try {
          String pin = cmd.getOptionValue("l");
          byte[] hexPin = pin.getBytes("UTF-8");
          
          Setup.getLog(hexPin);
        } catch (Exception e) {
          System.out.println(e);
        
        }
      } else if (cmd.hasOption("uap")) {
        try {
          String[] searchArgs = cmd.getOptionValues("uap");
          if (searchArgs.length == 2) {
            byte[] hexOldPin = searchArgs[0].getBytes("UTF-8");
            byte[] hexNewPin = searchArgs[1].getBytes("UTF-8");
          
            CardTerminal terminal = TerminalFactory.getDefault().terminals().list().get(0);            
            IdemixService is = new IdemixService(new TerminalCardService(terminal));
          
            is.open();
            is.updateCardPin(hexOldPin, hexNewPin);
          }
        } catch (Exception e) {
          System.out.println(e);
        
        }
      } else if (cmd.hasOption("ucp")) {
        try {
          String[] searchArgs = cmd.getOptionValues("ucp");
          if (searchArgs.length == 2) {
            byte[] hexAdminPin = searchArgs[0].getBytes("UTF-8");
            byte[] hexNewCredentialPin = searchArgs[1].getBytes("UTF-8");
          
            CardTerminal terminal = TerminalFactory.getDefault().terminals().list().get(0);            
            IdemixService is = new IdemixService(new TerminalCardService(terminal));
          
            is.open();

            is.sendCardPin(hexAdminPin);
            is.updateCredentialPin(hexNewCredentialPin);
          }
        } catch (Exception e) {
          System.out.println(e);
        
        }
       } else if (cmd.hasOption("ir")) {
        try {
          String pin = cmd.getOptionValue("ir");
          byte[] hexPin = pin.getBytes("UTF-8");
                    
          Setup.issueRootCredential(hexPin);
        } catch (Exception e) {
          System.out.println(e);
        
        }
       } else if (cmd.hasOption("vr")) {
        try {
          Setup.verifyRootCredentialAll();
        } catch (Exception e) {
          System.out.println(e);
        
        }
       } else if (cmd.hasOption("vrds")) {
        try {
          Setup.verifyRootCredentialAll_withDS();
        } catch (Exception e) {
          System.out.println(e);
        
        }
       } else if (cmd.hasOption("vrn")) {
        try {
          Setup.verifyRootCredentialNone();
        } catch (Exception e) {
          System.out.println(e);
        
        }
       } else if (cmd.hasOption("rr")) {
        try {
          String pin = cmd.getOptionValue("rr");
          byte[] hexPin = pin.getBytes("UTF-8");
 
          Setup.removeRootCredential(hexPin);
        } catch (Exception e) {
          System.out.println(e);
        
        }
       } else if (cmd.hasOption("is")) {
        try {
          String pin = cmd.getOptionValue("is");
          byte[] hexPin = pin.getBytes("UTF-8");
 
          Setup.issueStudentCredential(hexPin);
        } catch (Exception e) {
          System.out.println(e);
        
        }
       } else if (cmd.hasOption("vs")) {
        try {
          Setup.verifyStudentCredentialAll();
        } catch (Exception e) {
          System.out.println(e);
        
        }
       } else if (cmd.hasOption("vsn")) {
        try {
          Setup.verifyStudentCredentialNone();
        } catch (Exception e) {
          System.out.println(e);
        
        }
      } else if (cmd.hasOption("qc")) {
        try {
            CardTerminal terminal = TerminalFactory.getDefault().terminals().list().get(0);            
            IdemixService is = new IdemixService(new TerminalCardService(terminal));
          
            is.open();
            System.out.println(is.queryCredentialPin());
        } catch (Exception e) { 
          System.out.println(e);
        }
      } else if (cmd.hasOption("qa")) {
        try {
            CardTerminal terminal = TerminalFactory.getDefault().terminals().list().get(0);            
            IdemixService is = new IdemixService(new TerminalCardService(terminal));
          
            is.open();
            System.out.println(is.queryCardPin());
        } catch (Exception e) { 
          System.out.println(e);
        }
      } else if (cmd.hasOption("rs")) {
        try {
          String pin = cmd.getOptionValue("rs");
          byte[] hexPin = pin.getBytes("UTF-8");
 
          Setup.removeStudentCredential(hexPin);
        } catch (Exception e) {
          System.out.println(e);
        
        }

      } else if (cmd.hasOption("i")) {
        CardTerminal terminal = TerminalFactory.getDefault().terminals().list().get(0);            
        IdemixService is = new IdemixService(new TerminalCardService(terminal));
        is.open();
        CardVersion cv = is.getCardVersion();
        
        System.out.println("major: " + cv.getMajor() + " " + "minor: " + cv.getMinor());
        System.out.println("maint " + cv.getMaintenance());
        System.out.println("build " + cv.getBuild());
        System.out.println("count " + cv.getCounter());
        System.out.println("extra " + cv.getExtra());
        

      } else {
        showHelp(options);
      }
    } catch (Exception e) {
      showHelp(options);
    }
  }
}
