package client.corbasupport.RecordManagerApp;

/**
* RecordManagerApp/RecordManagerHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from RecordManager.idl
* Wednesday, July 26, 2017 9:37:57 AM EDT
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
