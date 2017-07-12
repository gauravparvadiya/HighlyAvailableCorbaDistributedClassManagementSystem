package frontend.corbasupport.RecordManagerApp;


/**
* RecordManagerApp/RecordManagerPOA.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from RecordManager.idl
* Sunday, July 9, 2017 10:11:20 PM EDT
*/

public abstract class RecordManagerPOA extends org.omg.PortableServer.Servant
 implements RecordManagerOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("createTRecord", new java.lang.Integer (0));
    _methods.put ("createSRecord", new java.lang.Integer (1));
    _methods.put ("getRecordCounts", new java.lang.Integer (2));
    _methods.put ("editRecord", new java.lang.Integer (3));
    _methods.put ("transferRecord", new java.lang.Integer (4));
    _methods.put ("crashLeadServer", new java.lang.Integer (5));
    _methods.put ("crashSecondaryServer", new java.lang.Integer (6));
  }

  public org.omg.CORBA.portable.OutputStream _invoke (String $method,
                                org.omg.CORBA.portable.InputStream in,
                                org.omg.CORBA.portable.ResponseHandler $rh)
  {
    org.omg.CORBA.portable.OutputStream out = null;
    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);
    if (__method == null)
      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

    switch (__method.intValue ())
    {
       case 0:  // RecordManagerApp/RecordManager/createTRecord
       {
         String mgrID = in.read_string ();
         String firstName = in.read_string ();
         String lastName = in.read_string ();
         String address = in.read_string ();
         String phone = in.read_string ();
         String specialization = in.read_string ();
         String location = in.read_string ();
         String $result = null;
         $result = this.createTRecord (mgrID, firstName, lastName, address, phone, specialization, location);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 1:  // RecordManagerApp/RecordManager/createSRecord
       {
         String mgrID = in.read_string ();
         String firstName = in.read_string ();
         String lastName = in.read_string ();
         String coursesRegistered = in.read_string ();
         String status = in.read_string ();
         String statusDate = in.read_string ();
         String $result = null;
         $result = this.createSRecord (mgrID, firstName, lastName, coursesRegistered, status, statusDate);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 2:  // RecordManagerApp/RecordManager/getRecordCounts
       {
         String mgrID = in.read_string ();
         String $result = null;
         $result = this.getRecordCounts (mgrID);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 3:  // RecordManagerApp/RecordManager/editRecord
       {
         String mgrID = in.read_string ();
         String recordID = in.read_string ();
         String fieldName = in.read_string ();
         String newValue = in.read_string ();
         String $result = null;
         $result = this.editRecord (mgrID, recordID, fieldName, newValue);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 4:  // RecordManagerApp/RecordManager/transferRecord
       {
         String mgrID = in.read_string ();
         String recordID = in.read_string ();
         String remServerName = in.read_string ();
         String $result = null;
         $result = this.transferRecord (mgrID, recordID, remServerName);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 5:  // RecordManagerApp/RecordManager/crashLeadServer
       {
         String $result = null;
         $result = this.crashLeadServer ();
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 6:  // RecordManagerApp/RecordManager/crashSecondaryServer
       {
         String $result = null;
         $result = this.crashSecondaryServer ();
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:RecordManagerApp/RecordManager:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public RecordManager _this() 
  {
    return RecordManagerHelper.narrow(
    super._this_object());
  }

  public RecordManager _this(org.omg.CORBA.ORB orb) 
  {
    return RecordManagerHelper.narrow(
    super._this_object(orb));
  }


} // class RecordManagerPOA
