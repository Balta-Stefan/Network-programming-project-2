/**
 * SOAP_service.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package mdp2021.backend.services.SOAP;

public interface SOAP_service extends java.rmi.Remote {
    public mdp2021.backend.shared.LoginReply login(java.lang.String username, java.lang.String password) throws java.rmi.RemoteException;
    public mdp2021.backend.shared.Code_response logout(java.lang.String cookie) throws java.rmi.RemoteException;
    public java.lang.String getTrainstationUsers(java.lang.String cookie) throws java.rmi.RemoteException;
}
