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
 *  CFEntryPair.java
 *  Copyright (C) 2009 Roberto Perdisci (roberto.perdisci@gmail.com)
 */

package org.streaminer.stream.clustering.birch;


/**
 * 
 * @author Roberto Perdisci (roberto.perdisci@gmail.com)
 *
 */
public class CFEntryPair {
	
	private static final String LINE_SEP = System.getProperty("line.separator");

	public CFEntry e1;
	public CFEntry e2;
	
	public CFEntryPair() {
	}
	
	public CFEntryPair(CFEntry e1, CFEntry e2) {
		this.e1 = e1;
		this.e2 = e2;
	}
	
	public boolean equals(Object o) {
		CFEntryPair p = (CFEntryPair)o;
		
		if(e1.equals(p.e1) && e2.equals(p.e2))
			return true;
		
		if(e1.equals(p.e2) && e2.equals(p.e1))
			return true;
		
		return false;
	}
	
	public String toString() {
		StringBuffer buff = new StringBuffer();
		
		buff.append("---- CFEntryPiar ----" + LINE_SEP);
		buff.append("---- e1 ----" + LINE_SEP);
		buff.append(e1.toString() + LINE_SEP);
		buff.append("---- e2 ----" + LINE_SEP);
		buff.append(e2.toString() + LINE_SEP);
		buff.append("-------- end --------" + LINE_SEP);
		
		return buff.toString();
	}
}
