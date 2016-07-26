package com.glookast.fimsclient.app;

import java.net.URL;

public class ServiceFactory
{
    public static Service createService(String name, Service.Type serviceType, Service.Method serviceMethod, URL address, URL callbackAddress)
    {
        switch (serviceType) {
            case Capture:
                switch (serviceMethod) {
                    case SOAP:
                        return new ServiceCaptureSOAP(name, address, callbackAddress);
                }
                break;
        }
        return null;
    }
}
