/**
 * LoginReply.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package mdp2021.backend.shared;

public class LoginReply  implements java.io.Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private mdp2021.backend.shared.Code_response codeResponse;

    private mdp2021.backend.model.TrainStation trainstationInfo;

    private java.lang.String cookie;

    public LoginReply() {
    }

    public LoginReply(
           mdp2021.backend.shared.Code_response codeResponse,
           mdp2021.backend.model.TrainStation trainstationInfo,
           java.lang.String cookie) {
           this.codeResponse = codeResponse;
           this.trainstationInfo = trainstationInfo;
           this.cookie = cookie;
    }


    /**
     * Gets the codeResponse value for this LoginReply.
     * 
     * @return codeResponse
     */
    public mdp2021.backend.shared.Code_response getCodeResponse() {
        return codeResponse;
    }


    /**
     * Sets the codeResponse value for this LoginReply.
     * 
     * @param codeResponse
     */
    public void setCodeResponse(mdp2021.backend.shared.Code_response codeResponse) {
        this.codeResponse = codeResponse;
    }


    /**
     * Gets the trainstationInfo value for this LoginReply.
     * 
     * @return trainstationInfo
     */
    public mdp2021.backend.model.TrainStation getTrainstationInfo() {
        return trainstationInfo;
    }


    /**
     * Sets the trainstationInfo value for this LoginReply.
     * 
     * @param trainstationInfo
     */
    public void setTrainstationInfo(mdp2021.backend.model.TrainStation trainstationInfo) {
        this.trainstationInfo = trainstationInfo;
    }


    /**
     * Gets the cookie value for this LoginReply.
     * 
     * @return cookie
     */
    public java.lang.String getCookie() {
        return cookie;
    }


    /**
     * Sets the cookie value for this LoginReply.
     * 
     * @param cookie
     */
    public void setCookie(java.lang.String cookie) {
        this.cookie = cookie;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof LoginReply)) return false;
        LoginReply other = (LoginReply) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.codeResponse==null && other.getCodeResponse()==null) || 
             (this.codeResponse!=null &&
              this.codeResponse.equals(other.getCodeResponse()))) &&
            ((this.trainstationInfo==null && other.getTrainstationInfo()==null) || 
             (this.trainstationInfo!=null &&
              this.trainstationInfo.equals(other.getTrainstationInfo()))) &&
            ((this.cookie==null && other.getCookie()==null) || 
             (this.cookie!=null &&
              this.cookie.equals(other.getCookie())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getCodeResponse() != null) {
            _hashCode += getCodeResponse().hashCode();
        }
        if (getTrainstationInfo() != null) {
            _hashCode += getTrainstationInfo().hashCode();
        }
        if (getCookie() != null) {
            _hashCode += getCookie().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(LoginReply.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://shared.backend.mdp2021", "LoginReply"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("codeResponse");
        elemField.setXmlName(new javax.xml.namespace.QName("http://shared.backend.mdp2021", "codeResponse"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://shared.backend.mdp2021", "Code_response"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("trainstationInfo");
        elemField.setXmlName(new javax.xml.namespace.QName("http://shared.backend.mdp2021", "trainstationInfo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.backend.mdp2021", "TrainStation"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("cookie");
        elemField.setXmlName(new javax.xml.namespace.QName("http://shared.backend.mdp2021", "cookie"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
