package application;


import java.awt.Font;
import java.awt.Image;
import java.awt.print.*;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PrinterResolution;

import impl.Fargo.*;



/**
 * @category Object which uses the FargoPrinterSDK.
 *           This is architecture independent implementation.
 *           Note: PrintJob class and XmlToDevmode functions cannot be used.
 */
public class IndependentCardProducer
{
	enum CardType
	{
		FRONT_SIDE_CARD,
		BACK_SIDE_CARD,
		BOTH_SIDE_CARD
	}


	// Name of the printer
	private String _printerName;

	// PrinterInfo object to read the Current Activity. 
	private PrinterInfo _printerInfo;


	// Constructor
	public IndependentCardProducer(String printerName)
	{
		// Copy the name of the printer.
		_printerName = printerName;

		// Create the PritnerInfo object used to get the CurrentActivity.
		_printerInfo = new PrinterInfo(printerName);
	}

	/**
	 * Create the card
	 * @throws Exception 
	 */
	public void produceCard(CardType cardType) throws Exception
	{
		// Check if we can print
		System.out.println("Check if printer is ready.");
		if (!waitForPrinterStatus(CurrentActivity.CurrentActivityReady, /*seconds*/3))
		{
			// This may be replaced by some waiting mechanism
			System.out.println("Printer is not ready - printing aborted.");
			return;
		}


		// Start preparing the card data
		System.out.println("Produce card...");


		// Adjust printer settings
		System.out.println("Adjust printer settings.");

		String settingsFilePath = getPrinterSettingsFilePath(cardType);
		if (!PrintingPreferencesSupport.LoadPrinterSettings(_printerName, settingsFilePath))
		{
			System.out.println("ERROR: Cannot adjust printer settings!");
			return;
		}


		// Create the card.
		//
		// IMPORTANT: Used Java API has limitation to 72dpi.
		//            The image is internally resized to for example 300dpi so it has lost of quality/resolution.
		//            TThe aim of this example is just to show how to print and monitor the printer on "mixed" platform.
		//            In the case when higher resolution is required, please use another Java API/package for printing.
		//            See: DocPrintJob class.
		//
		// NOTE: It is possible to use tilde command to encode a magnetic stripe(~1, ~2, ~3).
		//       All another features are also supported(~T, ~I, ~t, ~i).

		System.out.println("Load card images and texts and add them to the card.");
		Card card = new Card();

		// Front side elements
		if ((cardType == CardType.FRONT_SIDE_CARD) || (cardType == CardType.BOTH_SIDE_CARD))
		{
			// add image
			System.out.println("Load front side graphic.");

			try
			{
				Image frontSideImage = ImageIO.read(new File("72dpi_front.bmp"));
				if (frontSideImage != null)
					card.addImage(new ImagePrintElement(frontSideImage, 0, 0), Card.Side.FRONT);

			}
			catch (IOException e)
			{
				System.out.println("EXCEPTION ERROR: Cannot load front side image - " + e.getMessage());
				return;
			}

			// add text
			System.out.println("Create front side text.");
			TextPrintElement frontTextElement = new TextPrintElement("FRONT TEXT", new Font("TimesRoman", Font.PLAIN, 10), 10, 10 );
			card.addText(frontTextElement, Card.Side.FRONT);

			// examples of magnetic stripe encoding
			//TextPrintElement magTrack1 = new TextPrintElement("~2;ABCDE?", new Font("TimesRoman", Font.PLAIN, 10), 10, 10 );
			//card.addText(magTrack1, Card.Side.FRONT);
			//TextPrintElement magTrack2 = new TextPrintElement("~2;12345?", new Font("TimesRoman", Font.PLAIN, 10), 10, 10 );
			//card.addText(magTrack2, Card.Side.FRONT);
		}

		// Back side elements
		if ((cardType == CardType.BACK_SIDE_CARD) || (cardType == CardType.BOTH_SIDE_CARD))
		{
			// add image
			System.out.println("Load back side graphic.");

			try
			{
				Image backSideImage = ImageIO.read(new File("72dpi_back.bmp"));
				if (backSideImage != null)
					card.addImage(new ImagePrintElement(backSideImage, 0, 0), Card.Side.BACK);
			}
			catch (IOException e)
			{
				System.out.println("EXCEPTION ERROR: Cannot back front side image - " + e.getMessage());
				return;
			}

			// add text
			System.out.println("Create back side text.");
			TextPrintElement backTextElement = new TextPrintElement("BACK TEXT", new Font("TimesRoman", Font.PLAIN, 10), 10, 10 );
			card.addText(backTextElement, Card.Side.BACK);
		}


		// Create card printer and the print job.
		System.out.println("Create card printer.");

		CardPrinter cardPrinter = new CardPrinter(card, getPrintMode(cardType));
		PrinterJob job = PrinterJob.getPrinterJob();

		job.setPrintable(cardPrinter);
		job.setJobName("JAVA TEST PRINT JOB");

		PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();

		// set printer resolution to 300DPI
		aset.add(new PrinterResolution(300, 300, PrinterResolution.DPI));

		// set card size to CR-80(56mm x 87.7mm) with no margin
		aset.add(new MediaPrintableArea(0, 0, 56, 88, MediaPrintableArea.MM));

		// set card orientation to PORTRAIT
		//aset.add(OrientationRequested.LANDSCAPE);
		aset.add(OrientationRequested.PORTRAIT);


		// Note: It is possible to run dialog to adjust printing preferences
		//boolean doPrint = job.printDialog(aset);
		boolean doPrint = true;


		if (doPrint)
		{
			System.out.println("Run print job.");
			try
			{
				job.print(aset);

				// Wait for start printing
				if (!waitForPrinterStatus(CurrentActivity.CurrentActivityPrinting, /*seconds*/3))
				{
					throw new Exception("Printing start timeout or error");
				}

			} catch (PrinterException e) {
				System.out.println("EXCEPTION ERROR: Cannot print the card - " + e.getMessage());
				return;
			}

			// Wait for job to be done
			System.out.println("Wait for job to be done.");
			if (!waitForPrinterStatus(CurrentActivity.CurrentActivityReady, /*seconds*/20))
			{
				throw new Exception("Printing timeout or error");
			}

			System.out.println("DONE!");
		}
		else
		{
			System.out.println("Printing cancelled.");
		}
	}

	private String getPrinterSettingsFilePath(CardType cardType) throws Exception
	{
		switch(cardType)
		{
		case FRONT_SIDE_CARD:
			return new String("HDP5000_front_side_only.dat");
		case BACK_SIDE_CARD:
			return new String("HDP5000_back_side_only.dat");
		case BOTH_SIDE_CARD:
			return new String("HDP5000_both_sides.dat");
		default:
			throw new Exception("getPrinterSettingsFilePath(): Unsupported card type!");
		}
	}

	private CardPrinter.PrintMode getPrintMode(CardType cardType) throws Exception
	{
		// Files containing fixed printing properties.
		switch(cardType)
		{
		case FRONT_SIDE_CARD:
			return CardPrinter.PrintMode.FRONT_SIDE_ONLY;
		case BACK_SIDE_CARD:
			return CardPrinter.PrintMode.BACK_SIDE_ONLY;
		case BOTH_SIDE_CARD:
			return CardPrinter.PrintMode.BOTH_SIDES;
		default:
			throw new Exception("getPrintMode(): Unsupported card type!");
		}
	}

	private boolean waitForPrinterStatus(CurrentActivity expectedActivity, long timeoutSeconds) throws InterruptedException
	{
		long CHECK_INTERVAL = 250; // ms
		long attempts       = (1000 * timeoutSeconds) / CHECK_INTERVAL;

		while (true)
		{
			CurrentActivity currentActivity = _printerInfo.currentActivity();
			if (currentActivity == expectedActivity)
				break;

			if ((currentActivity == CurrentActivity.CurrentActivityException) ||
				(currentActivity == CurrentActivity.CurrentActivityUnknown))
			{
				return false; // error
			}

			if (attempts <= 0)
			{
				return false; // timeout
			}

			attempts--;
			Thread.sleep(CHECK_INTERVAL);
		}
		
		return true; // no timeout
	}
}