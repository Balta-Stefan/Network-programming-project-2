/**
 * SOAP_serviceServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package mdp2021.backend.services.SOAP;

public class SOAP_serviceServiceLocator extends org.apache.axis.client.Service implements mdp2021.backend.services.SOAP.SOAP_serviceService {

    public SOAP_serviceServiceLocator() {
    }


    public SOAP_serviceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public SOAP_serviceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for SOAP_service
    private java.lang.String SOAP_service_address = "http://localhost:8080/MDP2021_backend/services/SOAP_service";

    public java.lang.String getSOAP_serviceAddress() {
        return SOAP_service_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String SOAP_serviceWSDDServiceName = "SOAP_service";

    public java.lang.String getSOAP_serviceWSDDServiceName() {
        return SOAP_serviceWSDDServiceName;
    }

    public void setSOAP_serviceWSDDServiceName(java.lang.String name) {
        SOAP_serviceWSDDServiceName = name;
    }

    public mdp2021.backend.services.SOAP.SOAP_service getSOAP_service() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(SOAP_service_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getSOAP_service(endpoint);
    }

    public mdp2021.backend.services.SOAP.SOAP_service getSOAP_service(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            mdp2021.backend.services.SOAP.SOAP_serviceSoapBindingStub _stub = new mdp2021.backend.services.SOAP.SOAP_serviceSoapBindingStub(portAddress, this);
            _stub.setPortName(getSOAP_serviceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setSOAP_serviceEndpointAddress(java.lang.String address) {
        SOAP_service_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (mdp2021.backend.services.SOAP.SOAP_service.class.isAssignableFrom(serviceEndpointInterface)) {
                mdp2021.backend.services.SOAP.SOAP_serviceSoapBindingStub _stub = new mdp2021.backend.services.SOAP.SOAP_serviceSoapBindingStub(new java.net.URL(SOAP_service_address), this);
                _stub.setPortName(getSOAP_serviceWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("SOAP_service".equals(inputPortName)) {
            return getSOAP_service();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://SOAP.services.backend.mdp2021", "SOAP_serviceService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://SOAP.services.backend.mdp2021", "SOAP_service"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("SOAP_service".equals(portName)) {
            setSOAP_serviceEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
