/*******************************************************************************
 * Copyright (c) 2007, 2012 SpringSource
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource - initial API and implementation
 *******************************************************************************/

package org.eclipse.virgo.ide.bundlerepository.domain;

import java.util.StringTokenizer;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

/**
 * Persistable OSGi Version (with major, minor, service/micro and qualifier levels)
 */
public class OsgiVersion implements Comparable<OsgiVersion> {

    private int major;

    private int minor;

    private int service;

    private String qualifier = "";

    /* for persistence only */
    protected OsgiVersion() {
    }

    /**
     * Create an OsgiVersion from a Bundle by getting the BUNDLE_VERSION header
     *
     * @param bundle
     * @return an OSGiVersion object representing the version of the bundle
     */
    public static OsgiVersion ofBundle(Bundle bundle) {
        String headerValue = bundle.getHeaders().get(Constants.BUNDLE_VERSION);
        if (headerValue == null) {
            return new OsgiVersion(0, 0, 0, "");
        }
        return new OsgiVersion(headerValue.replaceAll("\"", ""));
    }

    /**
     * Build an OsgiVersion from its String representation. Acceptable forms are: 1 1.0 1.0.1 1.0.0.qualifier etc.
     *
     * @param the string version
     * @throws IllegalArgumentException
     */
    public OsgiVersion(String s) throws IllegalArgumentException {
        if (s == null) {
            throw new IllegalArgumentException("no version string specified");
        }

        s = s.trim();
        StringTokenizer tokenizer = new StringTokenizer(s, ".");
        if (!tokenizer.hasMoreTokens()) {
            throw new IllegalArgumentException("Can't convert '" + s + "' into an OSGi version");
        }

        try {
            this.major = Integer.parseInt(tokenizer.nextToken());
            this.minor = tokenizer.hasMoreTokens() ? Integer.parseInt(tokenizer.nextToken()) : 0;
            this.service = tokenizer.hasMoreTokens() ? Integer.parseInt(tokenizer.nextToken()) : 0;
            this.qualifier = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "";
        } catch (NumberFormatException nfEx) {
            throw new IllegalArgumentException("Can't convert '" + s + "' into an OSGi version");
        }
    }

    /**
     * Create an OsgiVersion explicitly specifying all levels.
     *
     * @param major
     * @param minor
     * @param service
     * @param qualifier
     */
    public OsgiVersion(int major, int minor, int service, String qualifier) {
        this.major = major;
        this.minor = minor;
        this.service = service;
        this.qualifier = null != qualifier ? qualifier : "";
    }

    /**
     * Create a persistable OsgiVersion from OSGi's own (non-persistent) Version type
     *
     * @param v
     */
    public OsgiVersion(Version v) {
        this.major = v.getMajor();
        this.minor = v.getMinor();
        this.service = v.getMicro();
        this.qualifier = null != v.getQualifier() ? v.getQualifier() : "";
    }

    public int getMajor() {
        return this.major;
    }

    public int getMinor() {
        return this.minor;
    }

    public int getService() {
        return this.service;
    }

    public String getQualifier() {
        return this.qualifier;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.major);
        builder.append(".");
        builder.append(this.minor);
        builder.append(".");
        builder.append(this.service);
        if (this.qualifier != null && !this.qualifier.equals("")) {
            builder.append(".");
            builder.append(this.qualifier);
        }
        return builder.toString();
    }

    /**
     * Orders as you would expect for major,minor,service, and alphabetically for qualifier.
     */
    public int compareTo(OsgiVersion o) {
        if (o == this) {
            return 0;
        }
        if (this.major > o.major) {
            return 1;
        } else if (this.major < o.major) {
            return -1;
        } else if (this.minor > o.minor) { // major versions equal
            return 1;
        } else if (this.minor < o.minor) {
            return -1;
        } else if (this.service > o.service) { // major and minor versions equal
            return 1;
        } else if (this.service < o.service) {
            return -1;
        } else { // major, minor, and service versions equals
            String myQualifier = this.qualifier;
            String otherQualifier = o.qualifier;
            if (myQualifier == null) {
                myQualifier = "";
            }
            if (otherQualifier == null) {
                otherQualifier = "";
            }
            return myQualifier.compareTo(otherQualifier);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof OsgiVersion)) {
            return false;
        }
        OsgiVersion version = (OsgiVersion) obj;
        return this.toString().equals(version.toString());
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

}
