/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.streaminer.util.hash;

/**
 * This class represents a common API for hashing functions.
 */
public abstract class Hash {
  /** Constant to denote invalid hash type. */
  public static final int INVALID_HASH = -1;
  /** Constant to denote {@link JenkinsHash}. */
  public static final int JENKINS_HASH = 0;
  /** Constant to denote {@link MurmurHash}. */
  public static final int MURMUR_HASH  = 1;
  /** Constant to denote {@link MurmurHash3}. */
  public static final int MURMUR_HASH3  = 2;
  
  public static final int SPOOKY_HASH  = 3;
  
  /**
   * This utility method converts String representation of hash function name
   * to a symbolic constant. Currently two function types are supported,
   * "jenkins" and "murmur".
   * @param name hash function name
   * @return one of the predefined constants
   */
  public static int parseHashType(String name) {
    if ("jenkins".equalsIgnoreCase(name)) {
      return JENKINS_HASH;
    } else if ("murmur".equalsIgnoreCase(name)) {
      return MURMUR_HASH;
    } else if ("murmur3".equalsIgnoreCase(name)) {
      return MURMUR_HASH3;
    } else {
      return INVALID_HASH;
    }
  }
  
  /**
   * Get a singleton instance of hash function of a given type.
   * @param type predefined hash type
   * @return hash function instance, or null if type is invalid
   */
  public static Hash getInstance(int type) {
    switch(type) {
    case JENKINS_HASH:
      return JenkinsHash.getInstance();
    case MURMUR_HASH:
      return MurmurHash.getInstance();
    case MURMUR_HASH3:
      return MurmurHash3.getInstance();
    case SPOOKY_HASH:
      return SpookyHash.getInstance();
    default:
      return null;
    }
  }
  
  protected static long seedLong() {
      return System.nanoTime();
  }
  
  protected static int seedInt() {
      return ((Long)System.nanoTime()).hashCode();
  }
  
  /**
   * Calculate a hash using all bytes from the input argument, and
   * current time as seed.
   * @param bytes input bytes
   * @return hash value
   */
  public int hash(byte[] bytes) {
    return hash(bytes, bytes.length, seedInt());
  }
  
  /**
   * Calculate a hash using all bytes from the input argument,
   * and a provided seed value.
   * @param bytes input bytes
   * @param seed seed value
   * @return hash value
   */
  public int hash(byte[] bytes, int seed) {
    return hash(bytes, bytes.length, seed);
  }
  
  /**
   * Calculate a hash using bytes from 0 to <code>length</code>, and
   * the provided seed value
   * @param bytes input bytes
   * @param length length of the valid bytes to consider
   * @param seed seed value
   * @return hash value
   */
  public abstract int hash(byte[] bytes, int length, int seed);
  
  /**
   * Calculate a hash using all bytes from the input argument, and
   * a seed of -1.
   * @param bytes input bytes
   * @return hash value
   */
  public long hash64(byte[] bytes) {
    return hash64(bytes, bytes.length, seedInt());
  }
  
  /**
   * Calculate a hash using all bytes from the input argument,
   * and a provided seed value.
   * @param bytes input bytes
   * @param seed seed value
   * @return hash value
   */
  public long hash64(byte[] bytes, int seed) {
    return hash64(bytes, bytes.length, seed);
  }
  
  /**
   * Calculate a hash using bytes from 0 to <code>length</code>, and
   * the provided seed value
   * @param bytes input bytes
   * @param length length of the valid bytes to consider
   * @param seed seed value
   * @return hash value
   */
  public abstract long hash64(byte[] bytes, int length, int seed);
  
  public abstract int hash(Object o);
  public abstract long hash64(Object o);
}