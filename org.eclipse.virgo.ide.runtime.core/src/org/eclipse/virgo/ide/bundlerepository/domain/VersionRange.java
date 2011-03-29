/*******************************************************************************
 * Copyright (c) 2007, 2009 SpringSource
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     SpringSource - initial API and implementation
 *******************************************************************************/
package org.eclipse.virgo.ide.bundlerepository.domain;

/**
 * Represents a version range as specified in an import package header
 * 
 */
public class VersionRange {

	public static final OsgiVersion INFINITY = new OsgiVersion(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE,
			"");

	private transient OsgiVersion lowerBound;

	private transient OsgiVersion upperBound;

	private boolean isInclusiveLowerBound = true; // if true then the a version == lowerBound will satisfy this range

	private boolean isInclusiveUpperBound = false; // if true then a version == upperBound will satisfy this range

	// these next shenanigans are because JPA can't persist an embeddable type nested in an embeddable type
	// therefore we have to either fudge things in the database (by storing version ranges in their own table), or
	// fudge things in the domain model (to allow version range and import package information to be stored in
	// the same table). We've chosen the latter option here as we never want to retrieve PackageImports without their
	// version ranges.
	private int lowerBoundMajor;

	private int lowerBoundMinor;

	private int lowerBoundService;

	private String lowerBoundQualifier;

	private int upperBoundMajor;

	private int upperBoundMinor;

	private int upperBoundService;

	private String upperBoundQualifier;

	protected VersionRange() {
	}

	/**
	 * Construct a version range from the String format found in the version attribute of an import package header
	 * @param osgiRangeSpecification range specification
	 */
	public VersionRange(String osgiRangeSpecification) {
		if ((osgiRangeSpecification == null) || (osgiRangeSpecification.equals(""))) {
			osgiRangeSpecification = "0.0.0";
		}
		// strip any surrounding quotes
		if (osgiRangeSpecification.startsWith("\"")) {
			osgiRangeSpecification = osgiRangeSpecification.substring(1, osgiRangeSpecification.length() - 1);
		}

		osgiRangeSpecification = extractLowerBoundCriteria(osgiRangeSpecification);

		osgiRangeSpecification = extractUpperBoundCriteria(osgiRangeSpecification);

		boolean isRange = (osgiRangeSpecification.indexOf(",") != -1);
		if (isRange) {
			int splitIndex = osgiRangeSpecification.indexOf(",");
			setLowerBound(new OsgiVersion(osgiRangeSpecification.substring(0, splitIndex)));
			try {
				setUpperBound(new OsgiVersion(osgiRangeSpecification.substring(splitIndex + 1)));
			}
			catch (IllegalArgumentException e) {
				// default to infinity
				setUpperBound(INFINITY);
			}
		}
		else {
			setLowerBound(new OsgiVersion(osgiRangeSpecification));
			setUpperBound(INFINITY);
		}
	}

	/**
	 * Create a version range object
	 * @param lowerBound lower bound of range
	 * @param lowerInclusive whether the lower bound is inclusive (true) or exclusive (false)
	 * @param upperBound upper bound of range
	 * @param upperInclusive whether the upper bound is inclusive (true) or exclusive (false)
	 */
	public VersionRange(OsgiVersion lowerBound, boolean lowerInclusive, OsgiVersion upperBound, boolean upperInclusive) {
		setLowerBound(lowerBound);
		setUpperBound(upperBound);
		this.isInclusiveLowerBound = lowerInclusive;
		this.isInclusiveUpperBound = upperInclusive;
	}

	/* shenanigans with version fields are because of JPA limitation */
	public OsgiVersion getLowerBound() {
		if (this.lowerBound == null) {
			this.lowerBound = new OsgiVersion(lowerBoundMajor, lowerBoundMinor, lowerBoundService, lowerBoundQualifier);
		}
		return this.lowerBound;
	}

	/* shenanigans with version fields are because of JPA limitation */
	public void setLowerBound(OsgiVersion bound) {
		this.lowerBound = bound;
		this.lowerBoundMajor = bound.getMajor();
		this.lowerBoundMinor = bound.getMinor();
		this.lowerBoundService = bound.getService();
		this.lowerBoundQualifier = bound.getQualifier();
	}

	/* shenanigans with version fields are because of JPA limitation */
	public OsgiVersion getUpperBound() {
		if (this.upperBound == null) {
			this.upperBound = new OsgiVersion(upperBoundMajor, upperBoundMinor, upperBoundService, upperBoundQualifier);
		}
		return this.upperBound;
	}

	public boolean isUpperBoundInfinity() {
		return INFINITY.equals(this.upperBound);
	}

	/* shenanigans with version fields are because of JPA limitation */
	public void setUpperBound(OsgiVersion bound) {
		this.upperBound = bound;
		this.upperBoundMajor = bound.getMajor();
		this.upperBoundMinor = bound.getMinor();
		this.upperBoundService = bound.getService();
		this.upperBoundQualifier = bound.getQualifier();
	}

	public boolean isInclusiveLowerBound() {
		return isInclusiveLowerBound;
	}

	public boolean isInclusiveUpperBound() {
		return isInclusiveUpperBound;
	}

	/**
	 * Return true if the given version falls within this range
	 */
	public boolean contains(OsgiVersion version) {
		// check lower bound
		int lowerBoundComparison = version.compareTo(getLowerBound());
		if (lowerBoundComparison < 0) {
			return false;
		}
		else if ((lowerBoundComparison == 0) && !this.isInclusiveLowerBound) {
			return false;
		}
		// we've passed the lower bound test, check the upper bound
		int upperBoundComparison = version.compareTo(getUpperBound());
		if (upperBoundComparison > 0) {
			return false;
		}
		else if ((upperBoundComparison == 0) && !this.isInclusiveUpperBound) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(isInclusiveLowerBound ? "[" : "(");
		sb.append(getLowerBound().toString());
		sb.append(", ");
		if (getUpperBound().equals(INFINITY)) {
			sb.append("infinity)");
		}
		else {
			sb.append(getUpperBound().toString());
			sb.append(isInclusiveUpperBound ? "]" : ")");
		}
		return sb.toString();
	}

	/* generated */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isInclusiveLowerBound ? 1231 : 1237);
		result = prime * result + (isInclusiveUpperBound ? 1231 : 1237);
		result = prime * result + lowerBoundMajor;
		result = prime * result + lowerBoundMinor;
		result = prime * result + ((lowerBoundQualifier == null) ? 0 : lowerBoundQualifier.hashCode());
		result = prime * result + lowerBoundService;
		result = prime * result + upperBoundMajor;
		result = prime * result + upperBoundMinor;
		result = prime * result + ((upperBoundQualifier == null) ? 0 : upperBoundQualifier.hashCode());
		result = prime * result + upperBoundService;
		return result;
	}

	/* generated */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final VersionRange other = (VersionRange) obj;
		if (isInclusiveLowerBound != other.isInclusiveLowerBound)
			return false;
		if (isInclusiveUpperBound != other.isInclusiveUpperBound)
			return false;
		if (lowerBoundMajor != other.lowerBoundMajor)
			return false;
		if (lowerBoundMinor != other.lowerBoundMinor)
			return false;
		if (lowerBoundQualifier == null) {
			if (other.lowerBoundQualifier != null)
				return false;
		}
		else if (!lowerBoundQualifier.equals(other.lowerBoundQualifier))
			return false;
		if (lowerBoundService != other.lowerBoundService)
			return false;
		if (upperBoundMajor != other.upperBoundMajor)
			return false;
		if (upperBoundMinor != other.upperBoundMinor)
			return false;
		if (upperBoundQualifier == null) {
			if (other.upperBoundQualifier != null)
				return false;
		}
		else if (!upperBoundQualifier.equals(other.upperBoundQualifier))
			return false;
		if (upperBoundService != other.upperBoundService)
			return false;
		return true;
	}

	/**
	 * Parse any inclusive "]" or exclusive ")" upper bound postfix and return the input string with them stripped
	 */
	private String extractUpperBoundCriteria(String osgiRangeSpecification) {
		// parse any ending inclusive / exclusive brackets
		if (osgiRangeSpecification.endsWith(")")) {
			this.isInclusiveUpperBound = false;
			osgiRangeSpecification = osgiRangeSpecification.substring(0, osgiRangeSpecification.length() - 1);
		}
		else if (osgiRangeSpecification.endsWith("]")) {
			this.isInclusiveUpperBound = true;
			osgiRangeSpecification = osgiRangeSpecification.substring(0, osgiRangeSpecification.length() - 1);
		}
		else {
			this.isInclusiveUpperBound = true;
		}
		return osgiRangeSpecification;
	}

	/**
	 * Parse any inclusive "[" or exclusive "(" lower bound prefix and return the input string with them stripped.
	 */
	private String extractLowerBoundCriteria(String osgiRangeSpecification) {
		// parse any opening inclusive / exclusive brackets
		if (osgiRangeSpecification.startsWith("(")) {
			this.isInclusiveLowerBound = false;
			osgiRangeSpecification = osgiRangeSpecification.substring(1);
		}
		else if (osgiRangeSpecification.startsWith("[")) {
			osgiRangeSpecification = osgiRangeSpecification.substring(1);
			this.isInclusiveLowerBound = true;
		}
		else {
			this.isInclusiveLowerBound = true;
		}
		return osgiRangeSpecification;
	}

}
