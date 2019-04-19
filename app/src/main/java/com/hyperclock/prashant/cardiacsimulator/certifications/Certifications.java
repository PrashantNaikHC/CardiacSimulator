package com.hyperclock.prashant.cardiacsimulator.certifications;

import java.util.HashMap;

public class Certifications {
    private String certificationName;
    private String certificationUrl;

    public Certifications(String certificationName, String certificationUrl) {
        this.certificationName = certificationName;
        this.certificationUrl = certificationUrl;
    }

    public String getCertificationName() {
        return certificationName;
    }

    public String getCertificationUrl() {
        return certificationUrl;
    }
}
