/**
 * SOAP_serviceService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package mdp2021.backend.services.SOAP;

public interface SOAP_serviceService extends javax.xml.rpc.Service {
    public java.lang.String getSOAP_serviceAddress();

    public mdp2021.backend.services.SOAP.SOAP_service getSOAP_service() throws javax.xml.rpc.ServiceException;

    public mdp2021.backend.services.SOAP.SOAP_service getSOAP_service(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
