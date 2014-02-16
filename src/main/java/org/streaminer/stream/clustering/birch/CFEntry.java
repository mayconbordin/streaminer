/*
 *  This file is part of JBIRCH.
 *
 *  JBIRCH is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  JBIRCH is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with JBIRCH.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/*
 *  CFNode.java
 *  Copyright (C) 2009 Roberto Perdisci (roberto.perdisci@gmail.com)
 */

package org.streaminer.stream.clustering.birch;


import java.util.*;

/**
 * 
 * @author Roberto Perdisci (roberto.perdisci@gmail.com)
 *
 */
public class CFEntry {
	
	private static final String LINE_SEP = System.getProperty("line.separator");
	
	private int n = 0; // number of patterns summarized by this entry
	private double[] sumX = null;
	private double[] sumX2 = null;
	private CFNode child = null;
	private ArrayList<Integer> indexList = null;
	private int subclusterID = -1; // the unique id the describes a subcluster (valid only for leaf entries)
	
	public CFEntry() {
	}
	
	public CFEntry(double[] x) {
		this(x,0);
	}
	
	public CFEntry(double[] x, int index) {
		this.n = 1;
		
		this.sumX = new double[x.length];
		for(int i=0; i<sumX.length; i++)
			sumX[i] = x[i];
		
		this.sumX2 = new double[x.length];
		for(int i=0; i<sumX2.length; i++)
			sumX2[i] = x[i]*x[i];
			
		indexList = new ArrayList<Integer>();
		indexList.add(index);
	}
	
	/**
	 * This makes a deep copy of the CFEntry e.
	 * WARNING: we do not make a deep copy of the child!!!
	 * 
	 * @param e the entry to be cloned
	 */
	public CFEntry(CFEntry e) {
		this.n = e.n;
		this.sumX = e.sumX.clone();
		this.sumX2 = e.sumX2.clone();
		this.child = e.child; // WARNING: we do not make a deep copy of the child!!!
		this.indexList = new ArrayList<Integer>();
		for(int i : e.getIndexList()) // this makes sure we get a deep copy of the indexList
			this.indexList.add(i);
	}
	
	protected ArrayList<Integer> getIndexList() {
		return indexList;
	}
	
	protected boolean hasChild() {
		return(child!=null);
	}
	
	protected CFNode getChild() {
		return child;
	}
	
	protected int getChildSize() {
		return child.getEntries().size();
	}
	
	protected void setChild(CFNode n) {
		child = n;
		indexList = null; // we don't keep this if this becomes a non-leaf entry
	}
	
	protected void setSubclusterID(int id) {
		subclusterID = id;
	}
	
	protected int getSubclusterID() {
		return subclusterID;
	}
	
	protected void update(CFEntry e) {
		this.n += e.n;
		
		if(this.sumX==null)
			this.sumX = e.sumX.clone();
		else {
			for(int i=0; i<sumX.length; i++) 
				this.sumX[i] += e.sumX[i];
		}
		
		if(this.sumX2==null)
			this.sumX2 = e.sumX2.clone();
		else {
			for(int i=0; i<sumX2.length; i++)
				this.sumX2[i] += e.sumX2[i];
		}
		
		if(!this.hasChild()) { // we keep indexList only if we are at a leaf
			if(this.indexList!=null && e.indexList!=null)
				this.indexList.addAll(e.indexList);
			else if(this.indexList==null && e.indexList!=null)
				this.indexList = (ArrayList<Integer>)e.indexList.clone();
		}
	}
	
	protected void addToChild(CFEntry e) {
		// adds directly to the child node
		child.getEntries().add(e);
	}
	
	protected boolean isWithinThreshold(CFEntry e, double threshold, int distFunction) {
		double dist = distance(e, distFunction);
		// System.out.println("Distance = " + dist);
		
		if(dist==0 || dist<=threshold) // read the comments in function d0() about differences with implementation in R
			return true;
		
		return false;
	}
	
	/**
	 * 
	 * @param e
	 * @return the distance between this entry and e
	 */
	protected double distance(CFEntry e, int distFunction) {
		double dist = Double.MAX_VALUE;
		
		switch(distFunction) {
		case CFTree.D0_DIST: 
			dist = d0(this,e);
			break;
		case CFTree.D1_DIST: 
			dist = d1(this,e);
			break;
		case CFTree.D2_DIST: 
			dist = d2(this,e);
			break;
		case CFTree.D3_DIST: 
			dist = d3(this,e);
			break;
		case CFTree.D4_DIST:
			dist = d4(this,e);
			break;
		}
		
		return dist;
	}
	
	private double d0(CFEntry e1, CFEntry e2) {
		double dist = 0;
		for(int i=0; i<e1.sumX.length; i++) {
			double diff = e1.sumX[i]/e1.n - e2.sumX[i]/e2.n;
			dist += diff*diff;
		}
		
		if(dist<0)
			System.err.println("d0 < 0 !!!");
		
		// notice here that in the R implementation of BIRCH (package birch)
		// 
		// the radius parameter is based on the squared distance /dist/
		// this causes a difference in results.
		// if we change the line below into 
		//   return dist;
		// the results produced by the R implementation and this Java implementation
		// will match perfectly (notice that in the R implementation maxEntries = 100
		// and merging refinement is not implemented)
		return Math.sqrt(dist);
	}
	
	private double d1(CFEntry e1, CFEntry e2) {
		double dist = 0;
		for(int i=0; i<e1.sumX.length; i++) {
			double diff = Math.abs(e1.sumX[i]/e1.n - e2.sumX[i]/e2.n);
			dist += diff;
		}
		
		if(dist<0)
			System.err.println("d1 < 0 !!!");
		
		return dist;
	}
	
	private double d2(CFEntry e1, CFEntry e2) {
		double dist = 0;
		
		int n1 = e1.n;
		int n2 = e2.n;
		for(int i=0; i<e1.sumX.length; i++) {
			double diff = (n2*e1.sumX2[i] - 2*e1.sumX[i]*e2.sumX[i] + n1*e2.sumX2[i])/(n1 * n2);
			dist += diff;
		}
		
		if(dist<0)
			System.err.println("d2 < 0 !!!");
		
		return Math.sqrt(dist);
	}

	private double d3(CFEntry e1, CFEntry e2) {
		double dist = 0;
		
		int n1 = e1.n;
		int n2 = e2.n;
		double[] totSumX = e1.sumX.clone();
		double[] totSumX2 = e1.sumX2.clone();
		for(int i=0; i<e2.sumX.length; i++) {
			totSumX[i] += e2.sumX[i];
			totSumX2[i] += e2.sumX2[i];
		}
		
		for(int i=0; i<totSumX.length; i++) {
			double diff = ((n1+n2)*totSumX2[i] - 2*totSumX[i]*totSumX[i] + (n1+n2)*totSumX2[i])/((n1+n2)*(n1+n2-1));
			dist += diff;
		}
		
		if(dist<0)
			System.err.println("d3 < 0 !!!");
		
		return Math.sqrt(dist);
	}
	
	private double d4(CFEntry e1, CFEntry e2) {
		double dist = 0;
		
		int n1 = e1.n;
		int n2 = e2.n;
		double[] totSumX = e1.sumX.clone();
		double[] totSumX2 = e1.sumX2.clone();
		for(int i=0; i<e2.sumX.length; i++) {
			totSumX[i] += e2.sumX[i];
			totSumX2[i] += e2.sumX2[i];
		}
		
		for(int i=0; i<totSumX.length; i++) {
			double diff1 = totSumX2[i] - 2*totSumX[i]*totSumX[i]/(n1+n2) + (n1+n2)*(totSumX[i]/(n1+n2))*(totSumX[i]/(n1+n2));
			double diff2 = e1.sumX2[i] - 2*e1.sumX[i]*e1.sumX[i]/n1 + n1*(e1.sumX[i]/n1)*(e1.sumX[i]/n1);
			double diff3 = e2.sumX2[i] - 2*e2.sumX[i]*e2.sumX[i]/n2 + n2*(e2.sumX[i]/n2)*(e2.sumX[i]/n2);
			dist += diff1 - diff2 - diff3;
		}
		
		if(dist<0)
			System.err.println("d4 < 0 !!!");
		
		return Math.sqrt(dist);
	}
	
	public boolean equals(Object o) {
		CFEntry e = (CFEntry)o;
		
		if(this.n != e.n)
			return false;
		
		if(this.child!=null && e.child==null)
			return false;
		
		if(this.child==null && e.child!=null)
			return false;
		
		if(this.child!=null && !this.child.equals(e.child))
			return false;
		
		if(this.indexList==null && e.indexList!=null)
			return false;
		
		if(this.indexList!=null && e.indexList==null)
			return false;
		
		if(!Arrays.equals(this.sumX, e.sumX))
			return false;
		
		if(!Arrays.equals(this.sumX2, e.sumX2))
			return false;
		
		if(this.indexList!=null && !this.indexList.equals(e.indexList))
			return false;
		
		return true;
	}
	
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append(" ");
		for(int i=0; i<sumX.length; i++)
			buff.append(sumX[i]/n + " ");
		
		if(this.indexList!=null) {
			buff.append("( ");
			for(int i : indexList) {
				buff.append(i+" ");
			}
			buff.append(")");
		}
		if(this.hasChild()) {
			buff.append(LINE_SEP);
			buff.append("||" + LINE_SEP);
			buff.append("||" + LINE_SEP);
			buff.append(this.getChild());
		}
		
		
		return buff.toString();
	}
}
