package RTi.DMI.NWSRFS_DMI;

import RTi.Util.IO.IOUtil;

import RTi.Util.Message.Message;

import RTi.Util.Time.StopWatch;

public class NWSRFS_Test {

private NWSRFS __nwsrfs = null;

private NWSRFS_DMI __dmi = null;

public NWSRFS_Test() {
	Message.setDebugLevel(Message.TERM_OUTPUT, 1);
	Message.setWarningLevel(Message.TERM_OUTPUT, 1);
	Message.setDebugLevel(Message.LOG_OUTPUT, 10);
	Message.setWarningLevel(Message.LOG_OUTPUT, 10);

	IOUtil.testing(true);

	try {
		__dmi = new NWSRFS_DMI();
		__nwsrfs = NWSRFS.createNWSRFSFromPRD(
			__dmi.getFS5FilesLocation());

		displayData();
	}
	catch (Exception e) {
		e.printStackTrace();
	}
}

public void displayData() 
throws Exception {
	NWSRFS_CarryoverGroup cg = null;
	NWSRFS_ForecastGroup fg = null;
	NWSRFS_Segment seg = null;
	NWSRFS_Operation op = null;
	NWSRFS_RatingCurve rc = null;

	StopWatch ssw = new StopWatch();
	StopWatch segsw = new StopWatch();

	String main_cg = "BPA";

	//make individual objects
	int numb_cgs = __nwsrfs.getNumberOfCarryoverGroups();

	int numFGs = -99;
	int numSegs = -99;
	int numOps = -99;
	String tsid = null;
	int numTSIDs = -99;
	int numRCs = -99;

	boolean printIDs = true;
	boolean printCounts = false;
	boolean printTimes = false;

	for (int icg = 0; icg < numb_cgs; icg++) {
		cg = __nwsrfs.getCarryoverGroup(icg);
		// Check the carryover group chosen at the beginning
		// if null then get all carryover groups (dangerous!)
		//NWSRFSGUI can ONLY have 1 carryovergroup.  SnowUpdating
		//GUI does not have that restriction.
		
		if (!cg.getCGID().equalsIgnoreCase(main_cg) && cg != null) {
			continue;
		}

		if (cg == null) {
			Message.printStatus(1, "", "Carryover group is null.");
			break;
		}
		else if (printIDs) {
			Message.printStatus(1, "", "[" + icg + "]: \""
				+ cg.getCGID() + "\"");
		}

		// Forecast Group
		numFGs = cg.getNumberOfForecastGroups();
		if (numFGs > 0 && printCounts) {
			Message.printStatus(1, "", "   Number of forecast groups: "
				+ numFGs);
		}

		for (int ifg = 0; ifg < numFGs; ifg++) {
			fg = cg.getForecastGroup(ifg);

			if (printIDs) {
				Message.printStatus(1, "", "   [" + ifg + "]: \"" 
					+ fg.getFGID() + "\"");
			}
			
			//Segments
			numSegs = fg.getNumberOfSegmentIDs();
			if (numSegs > 0 && printCounts) {
				Message.printStatus(1, "", "      Number of segments: "
					+ numSegs);
			}
		
			ssw.clear();
			ssw.start();
			for (int sfg = 0; sfg < numSegs; sfg++) {
				segsw.clear();
				segsw.start();
				seg = __dmi.readSegment(fg.getSegmentID(sfg), fg, false);
				segsw.stop();

				if (printIDs) {
					Message.printStatus(1, "", "      [" + sfg + "] : \"" 
						+ fg.getSegmentID(sfg) + "\"");
				}

				if (printTimes) {
					Message.printStatus(1, "", "      Seconds to read Segment: "
						+ segsw.getSeconds());
				}
		
				//Operations
				numOps = seg.getNumberOfOperations();
				if (numOps > 0 && printCounts) {
					Message.printStatus(1, "", "         Number of operations: "
						+ numOps);
				}
				
				for (int ofg = 0; ofg < numOps; ofg++) {
					op = seg.getOperation(ofg);

					if (printIDs) {
						Message.printStatus(1, "", "         [" + ofg 
							+ "]: \"" + op.getUserID() + "\"");
					}

					//Timeseries
					numTSIDs = op.getNumberOfTSIDs();
					if (numTSIDs > 0 && printCounts) {
						Message.printStatus(1, "", 
							"            Number of time series: " + numTSIDs);
					}
					
					for (int tsg = 0; tsg < numTSIDs; tsg++) {
						tsid = op.getTSID(tsg)
							+ ".NWSRFS."+op.getTSDT(tsg)
							+ "."+ (op.getTimeSeries(op.getTSID(tsg)))
							.getTSDTInterval() +"Hour";

						if (printIDs) {
							Message.printStatus(1, "", "            [" + tsg 
								+ "]: \"" + tsid + "\"");
						}
					}

					numRCs = op.getNumberOfRatingCurves();
					if (numRCs > 0 && printCounts) {
						Message.printStatus(1, "", 
							"            Number of rating curves: " + numRCs);
					}

					for (int rfg = 0; rfg < numRCs; rfg++ ) {
						rc = op.getRatingCurve(rfg);

						if (printIDs) {
							Message.printStatus(1, "", 
								"            [" + rfg + "]: \"" 
								+ rc.getRTCVID() + "\"");
						}
					}

				}
			}
			ssw.stop();

			if (printTimes) {
				Message.printStatus(1, "", "All segments: " + ssw.getSeconds());
			}
		}
	}

	Message.printStatus(1, "", 
		"\n\n\n"
		+ "parseOperationRecord exceptions: " + NWSRFS_DMI.parseOperationExceptionCount + "\n"
		+ "               other exceptions: " + NWSRFS_DMI.exceptionCount);
}

public static void main(String[] args) {
	new NWSRFS_Test();
}

}

// vim: ts=4
