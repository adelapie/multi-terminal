
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

public class IRMAClient 
{
   private static Options createOptions() {
    Options options = new Options();
    options.addOption("h", "help", false, "print this message and exit");
    options.addOption("i", "info-card", false, "get information about IRMA card");
    options.addOption("vcp", "verify-cred-pin", true, "verify credential pin status (4-digit)");
    options.addOption("vap", "verify-card-pin", true, "verify admin pin status (6-digit)");
    options.addOption("l", "log", true, "get log entries - it requires an admin pin (6-digit)");

    
    OptionBuilder.withArgName("old-pin new-pin");
    OptionBuilder.hasArgs(2);
    OptionBuilder.withDescription("update admin pin (6-digit)");

    Option updateCardPin = OptionBuilder.create("uap");
    options.addOption(updateCardPin);

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
      } else if (cmd.hasOption("i")) {
        Setup.readInfo();
      } else {
        showHelp(options);
      }
    } catch (Exception e) {
      showHelp(options);
    }
  }
}
