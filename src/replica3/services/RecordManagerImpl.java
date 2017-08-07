package replica3.services;

import replica3.entities.Record;
import replica3.entities.StudentRecord;
import replica3.entities.TeacherRecord;
import replica3.utilities.CenterServerUtil;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordManagerImpl {
	private int recIDCtr;
	private String fileName;
	private String centerID;
	private final Object recIDLock = new Object();
	private Map<Character, List<Record>> csRecs;
	private static final String FILEPATH = "D:\\Concordia\\Courses - Summer 2017\\COMP 6231 - DSD\\Project\\Logs\\replica3\\CenterServer\\";

	public RecordManagerImpl(String centerID) {
		this.centerID = centerID;
		csRecs = new HashMap<Character, List<Record>>();
		fileName = FILEPATH + centerID + ".txt";
		recIDCtr = 10000;
		createInitialRecords(centerID);
	}

	private void createInitialRecords(String centerID) {
		if (centerID.equalsIgnoreCase("MTL"))
			createInitialMtlRecords();
		else if (centerID.equalsIgnoreCase("LVL"))
			createInitialLvlRecords();
		else if (centerID.equalsIgnoreCase("DDO"))
			createInitialDdoRecords();
	}

	private void createInitialMtlRecords() {
		TeacherRecord tRec = new TeacherRecord("TR00001", "Joey", "Tribbiani", "34 Grove Street", "3548576788",
				"Performing Arts", "MTL");
		TeacherRecord gRec = new TeacherRecord("TR00002", "Monica", "Geller", "110 Main Street", "3548342315", "French",
				"DDO");
		StudentRecord bRec = null;
		try {
			bRec = new StudentRecord("SR00003", "Frank", "Buffay",
					new ArrayList<String>(Arrays.asList("Math", "French")), "active",
					new SimpleDateFormat("yyyy/MM/dd").parse("2016/08/28"));
		} catch (ParseException pe) {
		}

		List<Record> tRecList = new ArrayList<Record>();
		tRecList.add(tRec);
		csRecs.put('T', tRecList);

		List<Record> gRecList = new ArrayList<Record>();
		gRecList.add(gRec);
		csRecs.put('G', gRecList);

		List<Record> bRecList = new ArrayList<Record>();
		bRecList.add(bRec);
		csRecs.put('B', bRecList);
	}

	private void createInitialLvlRecords() {
		TeacherRecord gRec1 = new TeacherRecord("TR00004", "Ross", "Geller", "234 Woodwork Avenue", "8657456788",
				"Paleontology", "LVL");
		TeacherRecord gRec2 = new TeacherRecord("TR00005", "Rachel", "Green", "110 Main Street", "3548342879",
				"Fashion Design", "DDO");

		List<Record> gRecList = new ArrayList<Record>();
		gRecList.add(gRec1);
		gRecList.add(gRec2);
		csRecs.put('G', gRecList);
	}

	private void createInitialDdoRecords() {
		StudentRecord jRec = null;
		StudentRecord mRec = null;
		StudentRecord yRec = null;
		StudentRecord sRec = null;
		try {
			jRec = new StudentRecord("SR00006", "Tag", "Jones",
					new ArrayList<String>(Arrays.asList("Fashion Design", "Spanish")), "active",
					new SimpleDateFormat("yyyy/MM/dd").parse("2015/05/02"));
			mRec = new StudentRecord("SR00007", "Gavin", "Mitchell",
					new ArrayList<String>(Arrays.asList("Computer Science")), "inactive",
					new SimpleDateFormat("yyyy/MM/dd").parse("2017/04/12"));
			yRec = new StudentRecord("SR00008", "Ethan", "Young",
					new ArrayList<String>(Arrays.asList("Geology", "Zoology")), "active",
					new SimpleDateFormat("yyyy/MM/dd").parse("2016/12/02"));
			sRec = new StudentRecord("SR00009", "Mona", "Simmers",
					new ArrayList<String>(Arrays.asList("Arts", "Botany")), "inactive",
					new SimpleDateFormat("yyyy/MM/dd").parse("2015/05/02"));
		} catch (ParseException pe) {
		}

		List<Record> jRecList = new ArrayList<Record>();
		jRecList.add(jRec);
		csRecs.put('J', jRecList);

		List<Record> mRecList = new ArrayList<Record>();
		mRecList.add(mRec);
		csRecs.put('M', mRecList);

		List<Record> yRecList = new ArrayList<Record>();
		yRecList.add(yRec);
		csRecs.put('Y', yRecList);

		List<Record> sRecList = new ArrayList<Record>();
		sRecList.add(sRec);
		csRecs.put('S', sRecList);
	}

	public String createTRecord(String mgrID, String firstName, String lastName, String address, String phone,
			String specialization, String location) {
		String recID = null;
		String status = validateTeacher(firstName, lastName, address, phone, specialization, location);
		if (status == null) {
			synchronized (recIDLock) {
				recID = "TR" + recIDCtr;
				recIDCtr++;
			}
			TeacherRecord tRec = new TeacherRecord(recID, firstName, lastName, address, phone, specialization,
					location);
			boolean addStatus = addRec(tRec);
			if (!addStatus)
				status = "Teacher record creation failed";
			else
				status = "Teacher record creation successful";
		}

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String time = dateFormat.format(new Date());
		String log = "Create T Rec (ID: " + recID + " | FN: " + firstName + " | LN: " + lastName + " | ADR: " + address
				+ " | PH: " + phone + " | SPL: " + specialization + " | LOC: " + location + ") @" + time + " by "
				+ mgrID + " - " + status;
		CenterServerUtil.writeToFile(fileName, log);

		return status;
	}

	private String validateTeacher(String firstName, String lastName, String address, String phone, String specialization, String location) {
		String status = null;
		if ((firstName == null) || (firstName.trim().length() == 0))
			status = "First name cannot be null";
		else if ((lastName == null) || (lastName.trim().length() == 0))
			status = "Last name cannot be null";
		else if ((address == null) || (address.trim().length() == 0))
			status = "Address cannot be null";
		else if ((phone == null) || (phone.trim().length() == 0))
			status = "Phone number cannot be null";
		else if ((specialization == null) || (specialization.trim().length() == 0))
			status = "Specialization cannot be null";
		else if ((location == null) || (location.trim().length() == 0))
			status = "Location cannot be null";
		else if (!((location.equalsIgnoreCase("MTL")) || (location.equalsIgnoreCase("LVL"))
				|| (location.equalsIgnoreCase("DDO"))))
			status = "Invalid location value for teacher";
		else {
			boolean isNum = true;
			for (int i = 0; i < phone.length(); i++) {
				char digit = phone.charAt(i);
				if (!((digit >= '0') && (digit <= '9')))
					isNum = false;
			}
			if (!isNum)
				status = "Invalid phone number value for teacher";
		}
		return status;
	}

	public boolean addRec(Record rec) {
		String lastName = rec.getLastName();
		char key = lastName.charAt(0);
		key = Character.toUpperCase(key);
		List<Record> listFound = null;
		synchronized (csRecs) {
			listFound = csRecs.get(key);

			if (listFound == null) {
				List<Record> newList = new ArrayList<Record>();
				newList.add(rec);
				csRecs.put(key, newList);
			}
		}
		if (listFound != null) {
			synchronized (listFound) {
				if (!listFound.contains(rec))
					listFound.add(rec);
			}
		}
		return true;
	}

	public String createSRecord(String mgrID, String firstName, String lastName, String coursesRegistered, String stat,
			String statusDate) {
		String recID = null;
		List<String> courses = Arrays.asList(coursesRegistered.split(","));
		String status = validateStudent(firstName, lastName, courses, stat, statusDate);

		if (status == null) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
			Date date = null;
			try {
				date = dateFormat.parse(statusDate);
			} catch (ParseException pe) {
			}

			synchronized (recIDLock) {
				recID = "SR" + recIDCtr;
				recIDCtr++;
			}
			StudentRecord sRec = new StudentRecord(recID, firstName, lastName, courses, stat, date);
			boolean addStatus = addRec(sRec);
			if (!addStatus)
				status = "Student record creation failed";
			else
				status = "Student record creation successful";
		}

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String time = dateFormat.format(new Date());
		String log = "Create S Rec (ID: " + recID + " | FN: " + firstName + " | LN: " + lastName + " | CR: " + courses
				+ " | ST: " + stat + " | SDT: " + statusDate + ") @" + time + " by " + mgrID + " - " + status;
		CenterServerUtil.writeToFile(fileName, log);

		return status;
	}

	private String validateStudent(String firstName, String lastName, List<String> coursesRegistered, String status,
			String statusDate) {
		String validationFailure = null;

		if ((firstName == null) || (firstName.trim().length() == 0))
			validationFailure = "First name cannot be null";
		else if ((lastName == null) || (lastName.trim().length() == 0))
			validationFailure = "Last name cannot be null";
		else if ((coursesRegistered == null) || (coursesRegistered.isEmpty()))
			validationFailure = "Course list cannot be null";
		else if ((status == null) || (status.trim().length() == 0))
			validationFailure = "Status cannot be null";
		else if ((statusDate == null) || (statusDate.trim().length() == 0))
			validationFailure = "Status date cannot be null";
		else if (!((status.equalsIgnoreCase("active")) || (status.equalsIgnoreCase("inactive"))))
			validationFailure = "Invalid status value for student";
		else {
			for (String course : coursesRegistered)
				if ((course == null) || (course.trim().length() == 0)) {
					validationFailure = "Course name cannot be null";
					break;
				}

			if (validationFailure == null) {
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
				try {
					Date newDate = dateFormat.parse(statusDate);
					String newParsedDateStr = dateFormat.format(newDate);
					boolean dtFormatCorrect = statusDate.equalsIgnoreCase(newParsedDateStr);
					if (!dtFormatCorrect)
						validationFailure = "Invalid status date format";
				} catch (ParseException pe) {
					validationFailure = "Value for status date is not a date";
				}
			}
		}

		return validationFailure;
	}

	public String getRecordCounts(String mgrID) {
		int i = 0;
		Map<String, List<String>> servHostPorts = new HashMap<String, List<String>>();
		servHostPorts.put("MTL", new ArrayList<String>(Arrays.asList("localhost", "6799")));
		servHostPorts.put("LVL", new ArrayList<String>(Arrays.asList("localhost", "6800")));
		servHostPorts.put("DDO", new ArrayList<String>(Arrays.asList("localhost", "6801")));
		Map<String, Integer> servCts = new HashMap<String, Integer>();
		for (Map.Entry<String, List<String>> entry : servHostPorts.entrySet())
			servCts.put(entry.getKey(), -1);

		RecCtrClientThread[] recCtrs = new RecCtrClientThread[servCts.size() - 1];
		for (Map.Entry<String, List<String>> entry : servHostPorts.entrySet()) {
			if (!(entry.getKey().equalsIgnoreCase(centerID))) {
				String host = entry.getValue().get(0);
				int port = Integer.parseInt(entry.getValue().get(1));
				recCtrs[i] = new RecCtrClientThread(servCts, entry.getKey(), host, port);
				recCtrs[i].start();
				i++;
			}
		}

		int ownRecCount = getOwnRecordCount();
		servCts.put(centerID.toUpperCase(), ownRecCount);
		try {
			for (int j = 0; j < recCtrs.length; j++)
				recCtrs[j].join();
		} catch (InterruptedException ie) {
			System.out.println("Exception in getRecordCounts: " + ie.getMessage());
		}
		String finalCt = "Successful - ";
		for (Map.Entry<String, Integer> entry : servCts.entrySet())
			finalCt = finalCt + entry.getKey() + " " + entry.getValue() + ", ";
		finalCt = finalCt.trim().substring(0, finalCt.lastIndexOf(","));

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String time = dateFormat.format(new Date());
		String log = "Get Rec Count @" + time + " by " + mgrID + " - " + finalCt;
		CenterServerUtil.writeToFile(fileName, log);

		return finalCt;
	}

	public int getOwnRecordCount() {
		int count = 0;

		for (Map.Entry<Character, List<Record>> entry : csRecs.entrySet()) {
			List<Record> list = entry.getValue();
			int listCt = list.size();
			count += listCt;
		}

		return count;
	}

	public String editRecord(String mgrID, String recID, String fieldName, String newValue) {
		String status = "Edit successful";
		if ((recID == null) || (recID.trim().length() == 0))
			status = "Record ID cannot be null";
		else if ((fieldName == null) || (fieldName.trim().length() == 0))
			status = "Field name cannot be null";
		else if (newValue == null)
			status = "New value cannot be null";
		else {
			List<Record> listFound = locList(recID);
			if (listFound == null)
				status = "No matching record found";
			else {
				synchronized (listFound) {
					Record recFound = locRec(recID);

					if (recFound == null)
						status = "No matching record found";
					else {
						if (recID.substring(0, 1).equalsIgnoreCase("T")) {
							if (newValue.trim().length() == 0)
								status = "New value cannot be null";
							else {
								TeacherRecord tRec = (TeacherRecord) recFound;
								status = editTRec(fieldName, newValue, tRec);
							}
						} else if (recID.substring(0, 1).equalsIgnoreCase("S")) {
							StudentRecord sRec = (StudentRecord) recFound;
							status = editSRec(fieldName, newValue, sRec);
						} else
							status = "Invalid record ID";
					}
				}
			}
		}

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String time = dateFormat.format(new Date());
		String log = "Edit Rec (ID: " + recID + " | FLD: " + fieldName + " | VAL: " + newValue + ") @" + time + " by "
				+ mgrID + " - " + status;
		CenterServerUtil.writeToFile(fileName, log);

		return status;
	}

	private List<Record> locList(String recID) {
		List<Record> listFound = null;
		for (Map.Entry<Character, List<Record>> entry : csRecs.entrySet()) {
			List<Record> list = entry.getValue();
			for (Record rec : list) {
				if (rec.getRecordID().equalsIgnoreCase(recID)) {
					listFound = list;
					break;
				}
			}
			if (listFound != null)
				break;
		}
		return listFound;
	}

	private Record locRec(String recID) {
		Record recFound = null;
		for (Map.Entry<Character, List<Record>> entry : csRecs.entrySet()) {
			List<Record> list = entry.getValue();
			for (Record rec : list) {
				if (rec.getRecordID().equalsIgnoreCase(recID)) {
					recFound = rec;
					break;
				}
			}
			if (recFound != null)
				break;
		}
		return recFound;
	}

	private String editTRec(String fieldName, String newValue, TeacherRecord tRec) {
		String status = null;
		if (fieldName.equalsIgnoreCase("location")) {
			if (!((newValue.equalsIgnoreCase("MTL")) || (newValue.equalsIgnoreCase("LVL"))
					|| (newValue.equalsIgnoreCase("DDO"))))
				status = "Invalid location value for teacher";
			else {
				tRec.setLocation(newValue.toUpperCase());
				status = "Edit successful";
			}
		} else if (fieldName.equalsIgnoreCase("address")) {
			tRec.setAddress(newValue);
			status = "Edit successful";
		} else if (fieldName.equalsIgnoreCase("phone")) {
			boolean isNum = true;
			for (int i = 0; i < newValue.length(); i++) {
				char digit = newValue.charAt(i);
				if (!((digit >= '0') && (digit <= '9')))
					isNum = false;
			}
			if (isNum) {
				tRec.setPhone(newValue);
				status = "Edit successful";
			} else
				status = "Invalid phone number value for teacher";
		} else
			status = "Field " + fieldName + " is not editable for teacher";

		return status;
	}

	private String editSRec(String fieldName, String newValue, StudentRecord sRec) {
		String status = null;
		if (fieldName.equalsIgnoreCase("courses_registered")) {
			List<String> newCourses = Arrays.asList(newValue.split(","));
			if (newCourses.isEmpty())
				status = "New course list cannot be empty";
			else {
				for (String course : newCourses)
					if ((course == null) || (course.trim().length() == 0)) {
						status = "Course name cannot be null";
						break;
					}
				if (status == null) {
					sRec.setCoursesRegistered(newCourses);
					status = "Edit successful";
				}
			}
		} else if (fieldName.equalsIgnoreCase("status")) {
			if (newValue.trim().length() == 0)
				status = "New value cannot be null";
			else if (!((newValue.equalsIgnoreCase("active")) || (newValue.equalsIgnoreCase("inactive"))))
				status = "Invalid status value for student";
			else {
				String currStatus = sRec.getStatus();
				String newStatus = newValue.toLowerCase();
				if (!(currStatus.equalsIgnoreCase(newStatus))) {
					sRec.setStatus(newStatus);
					sRec.setStatusDate(new Date());
					status = "Edit successful";
				} else
					status = "Student is already " + newStatus;
			}
		} else if (fieldName.equalsIgnoreCase("status_date")) {
			if (newValue.trim().length() == 0)
				status = "New value cannot be null";
			else {
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
				Date newDate = null;
				try {
					newDate = dateFormat.parse(newValue);
					String newParsedDateStr = dateFormat.format(newDate);
					boolean dtFormatCorrect = newValue.equalsIgnoreCase(newParsedDateStr);
					if (!dtFormatCorrect)
						status = "Invalid status date format";
				} catch (ParseException pe) {
					status = "New value for status date is not a date";
				}

				if (status == null) {
					sRec.setStatusDate(newDate);
					status = "Edit successful";
				}
			}
		} else
			status = "Field " + fieldName + " is not editable for student";

		return status;
	}

	public String transferRecord(String mgrID, String recID, String remServerName) {
		String status = null;
		if ((recID == null) || (recID.trim().length() == 0))
			status = "Record ID cannot be null";
		else if ((remServerName == null) || (remServerName.trim().length() == 0))
			status = "Remote server name cannot be null";
		else if ((!((remServerName.equalsIgnoreCase("MTL")) || (remServerName.equalsIgnoreCase("LVL"))
				|| (remServerName.equalsIgnoreCase("DDO")))))
			status = "Invalid remote server name";
		else if (remServerName.equalsIgnoreCase(centerID))
			status = "Source and destination servers cannot be the same";

		if (status == null) {
			List<Record> listFound = locList(recID);
			if (listFound == null)
				status = "No matching record found";
			else {
				synchronized (listFound) {
					Record recFound = locRec(recID);

					if (recFound == null)
						status = "No matching record found";
					else {
						String destHost = "localhost";
						String port = null;
						if (centerID.equalsIgnoreCase("MTL"))
							port = "6799";
						else if (centerID.equalsIgnoreCase("LVL"))
							port = "6800";
						else if (centerID.equalsIgnoreCase("DDO"))
							port = "6801";
						int destPort = Integer.parseInt(port);

						RecTransferClientThread transferClient = new RecTransferClientThread(recFound, destHost,
								destPort);
						transferClient.start();
						try {
							transferClient.join();
						} catch (InterruptedException ie) {
							System.out.println("Exception in transferRecord: " + ie.getMessage());
						}
						status = transferClient.getStatus();
						if (status.toLowerCase().indexOf("success") >= 0) {
							removeRecord(recFound);
							status = "Transfer successful";
						}
					}
				}
			}
		}

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String time = dateFormat.format(new Date());
		String log = "Transfer Rec (ID: " + recID + " | SRV: " + remServerName + ") @" + time + " by " + mgrID + " - "
				+ status;
		CenterServerUtil.writeToFile(fileName, log);

		return status;
	}

	private void removeRecord(Record record) {
		List<Record> listFound = null;
		String recID = record.getRecordID();
		for (Map.Entry<Character, List<Record>> entry : csRecs.entrySet()) {
			List<Record> list = entry.getValue();
			for (Record rec : list) {
				if (rec.getRecordID().equalsIgnoreCase(recID)) {
					listFound = list;
					break;
				}
			}
			if (listFound != null)
				break;
		}
		if (listFound != null) {
			int pos = listFound.indexOf(record);
			listFound.remove(pos);
		}
	}
}