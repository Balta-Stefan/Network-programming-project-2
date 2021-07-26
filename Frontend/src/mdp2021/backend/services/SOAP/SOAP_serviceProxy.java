package mdp2021.backend.services.SOAP;

public class SOAP_serviceProxy implements mdp2021.backend.services.SOAP.SOAP_service {
  private String _endpoint = null;
  private mdp2021.backend.services.SOAP.SOAP_service sOAP_service = null;
  
  public SOAP_serviceProxy() {
    _initSOAP_serviceProxy();
  }
  
  public SOAP_serviceProxy(String endpoint) {
    _endpoint = endpoint;
    _initSOAP_serviceProxy();
  }
  
  private void _initSOAP_serviceProxy() {
    try {
      sOAP_service = (new mdp2021.backend.services.SOAP.SOAP_serviceServiceLocator()).getSOAP_service();
      if (sOAP_service != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)sOAP_service)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)sOAP_service)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (sOAP_service != null)
      ((javax.xml.rpc.Stub)sOAP_service)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public mdp2021.backend.services.SOAP.SOAP_service getSOAP_service() {
    if (sOAP_service == null)
      _initSOAP_serviceProxy();
    return sOAP_service;
  }
  
  public mdp2021.backend.shared.LoginReply login(java.lang.String username, java.lang.String password) throws java.rmi.RemoteException{
    if (sOAP_service == null)
      _initSOAP_serviceProxy();
    return sOAP_service.login(username, password);
  }
  
  public mdp2021.backend.shared.Code_response logout(java.lang.String cookie) throws java.rmi.RemoteException{
    if (sOAP_service == null)
      _initSOAP_serviceProxy();
    return sOAP_service.logout(cookie);
  }
  
  public java.lang.String getTrainstationUsers(java.lang.String cookie) throws java.rmi.RemoteException{
    if (sOAP_service == null)
      _initSOAP_serviceProxy();
    return sOAP_service.getTrainstationUsers(cookie);
  }
  
  
}