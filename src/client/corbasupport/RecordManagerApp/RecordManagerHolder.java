package client.corbasupport.RecordManagerApp;

/**
* RecordManagerApp/RecordManagerHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from RecordManager.idl
* Sunday, July 9, 2017 10:07:55 PM EDT
*/

public final class RecordManagerHolder implements org.omg.CORBA.portable.Streamable
{
  public RecordManager value = null;

  public RecordManagerHolder ()
  {
  }

  public RecordManagerHolder (RecordManager initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = RecordManagerHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    RecordManagerHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return RecordManagerHelper.type ();
  }

}
