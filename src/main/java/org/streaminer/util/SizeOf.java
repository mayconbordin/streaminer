package org.streaminer.util;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.instrument.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * A simple implementation of C++ keyword sizeof for Java 5+  
 *  
 * @author Nicola Santi
 * @author Marco Rosi
 * 
 * @see java#lang#instrument#Instrument
 * 
 * @version 0.6
 */
public class SizeOf
{
	private static OutputStream out = System.out;

	/**
	 * Instance of java.lang.instrument.Instrument injected by the Java VM 
	 * @see premain(String options, Instrumentation inst) 
	 */
	private static Instrumentation inst;

	private static long MIN_CLASS_SIZE_TO_LOG = 1024 * 1024;
	private static boolean SKIP_STATIC_FIELD = false;
	private static boolean SKIP_FINAL_FIELD = false;
	private static boolean SKIP_FLYWEIGHT_FIELD = false;
	private static boolean debug = false;

	/**
	 * Callback method used by the Java VM to inject the java.lang.instrument.Instrument
	 * instance
	 */
	public static void premain(String options, Instrumentation inst)
	{
		SizeOf.inst = inst;

		System.out.println("JAVAGENT: call premain instrumentation for class SizeOf");
	}

	/**
	 * Calls java.lang.instrument.Instrument.getObjectSize(object).
	 *  
	 * @param object the object to size
	 * @return an implementation-specific approximation of the amount of storage consumed 
	 * 	by the specified object.  
	 * 
	 * @see java#lang#instrument#Instrument#Instrumentation#getObjectSize(Object objectToSize)
	 */
	public static long sizeOf(Object object)
	{
		if (inst == null)
			throw new IllegalStateException("Instrumentation is null");

		if (SKIP_FLYWEIGHT_FIELD && isSharedFlyweight(object))
			return 0;

		return inst.getObjectSize(object);
	}

	private static String[] unit = { "b", "Kb", "Mb" };

	/**
	 * Format size in a human readable format
	 * 
	 * @param size
	 * @return a string representation of the size argument followed by
	 * 	b for byte, Kb for kilobyte or Mb for megabyte 
	 */
	public static String humanReadable(long size)
	{
		int i;
		double dSize = size;
		for (i = 0; i < 3; ++i)
		{
			if (dSize < 1024)
				break;
			dSize /= 1024;
		}

		return dSize + unit[i];
	}

	/**
	 * Compute an implementation-specific approximation of the amount of storage consumed 
	 * by objectToSize and by all the objects reachable from it 
	 * 
	 * @param objectToSize
	 * @return an implementation-specific approximation of the amount of storage consumed 
	 * 	by objectToSize and by all the objects reachable from it
	 */
	public static long deepSizeOf(Object objectToSize)
	{
		//Set<Integer> doneObj = new HashSet<Integer>();
		Map<Object,Object> doneObj = new IdentityHashMap<Object,Object>();
		return deepSizeOf(objectToSize, doneObj, 0);
	}

	/**
	 * @deprecated use deepSizeOf
	 */
	public static long iterativeSizeOf(Object objectToSize) throws IllegalArgumentException,
			IllegalAccessException, IOException
	{
		return deepSizeOf(objectToSize);
	}

	private static String indent(int depth)
	{
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < depth; i++)
			builder.append("  ");

		return builder.toString();
	}

	private static long deepSizeOf(Object o, Map<Object,Object> doneObj, int depth)
	{
		if (o == null)
		{
			if (debug)
				print("null\n");
			return 0;
		}

		long size = 0;

		if (doneObj.containsKey(o))
		{
			if (debug)
				print("\n%s{ yet computed }\n", indent(depth));
			return 0;
		}

		if (debug)
			print("\n%s{ %s\n", indent(depth), o.getClass().getName());

		doneObj.put(o,null);
		size = sizeOf(o);

		if (o instanceof Object[])
		{
			int i = 0;
			for (Object obj : (Object[]) o)
			{
				if (debug)
					print("%s [%d] = ", indent(depth), i++);
				size += deepSizeOf(obj, doneObj, depth + 1);
			}
		} else
		{

			Field[] fields = o.getClass().getDeclaredFields();

			for (Field field : fields)
			{
				field.setAccessible(true);
				Object obj;
				try
				{
					obj = field.get(o);
				} catch (IllegalArgumentException e)
				{
					throw new RuntimeException(e);
				} catch (IllegalAccessException e)
				{
					throw new RuntimeException(e);
				}

				if (isComputable(field))
				{
					if (debug)
						print("%s %s = ", indent(depth), field.getName());
					size += deepSizeOf(obj, doneObj, depth + 1);
				} else
				{
					if (debug)
						print("%s %s = %s\n", indent(depth), field.getName(), obj.toString());
				}
			}
		}

		if (debug)
			print("%s} size = %s\n", indent(depth), humanReadable(size));

		if (MIN_CLASS_SIZE_TO_LOG > 0 && size >= MIN_CLASS_SIZE_TO_LOG)
			print("Found big object: %s%s@%s size: %s\n", indent(depth), o.getClass().getName(), System.identityHashCode(o),
					humanReadable(size));

		return size;
	}

	/**
	 * Return true if the specified class is a primitive type
	 */
	private static boolean isAPrimitiveType(Class clazz)
	{
		if (clazz == java.lang.Boolean.TYPE)
			return true;

		if (clazz == java.lang.Character.TYPE)
			return true;

		if (clazz == java.lang.Byte.TYPE)
			return true;

		if (clazz == java.lang.Short.TYPE)
			return true;

		if (clazz == java.lang.Integer.TYPE)
			return true;

		if (clazz == java.lang.Long.TYPE)
			return true;

		if (clazz == java.lang.Float.TYPE)
			return true;

		if (clazz == java.lang.Double.TYPE)
			return true;

		if (clazz == java.lang.Void.TYPE)
			return true;

		return false;
	}

	/**
	 * @param field
	 * @param obj 
	 * @return true if the field must be computed
	 */
	private static boolean isComputable(Field field)
	{
		int modificatori = field.getModifiers();

		if (isAPrimitiveType(field.getType()))
			return false;
		else if (SKIP_STATIC_FIELD && Modifier.isStatic(modificatori))
			return false;
		else if (SKIP_FINAL_FIELD && Modifier.isFinal(modificatori))
			return false;
		else
			return true;
	}

	/**
	 * Returns true if this is a well-known shared flyweight.
	 * For example, interned Strings, Booleans and Number objects.
	 * 
	 * thanks to Dr. Heinz Kabutz 
	 * see http://www.javaspecialists.co.za/archive/Issue142.html
	 */
	private static boolean isSharedFlyweight(Object obj)
	{
		// optimization - all of our flyweights are Comparable
		if (obj instanceof Comparable)
		{
			if (obj instanceof Enum)
			{
				return true;
			} else if (obj instanceof String)
			{
				return (obj == ((String) obj).intern());
			} else if (obj instanceof Boolean)
			{
				return (obj == Boolean.TRUE || obj == Boolean.FALSE);
			} else if (obj instanceof Integer)
			{
				return (obj == Integer.valueOf((Integer) obj));
			} else if (obj instanceof Short)
			{
				return (obj == Short.valueOf((Short) obj));
			} else if (obj instanceof Byte)
			{
				return (obj == Byte.valueOf((Byte) obj));
			} else if (obj instanceof Long)
			{
				return (obj == Long.valueOf((Long) obj));
			} else if (obj instanceof Character)
			{
				return (obj == Character.valueOf((Character) obj));
			}
		}
		return false;
	}

	/**
	 * The objects processed by deepSizeOf are logged to the log output stream if
	 * their size (in byte) is greater than this value.
	 * The default value is 1024*1024 (1 megabyte), 0 turn off logging
	 */
	public static void setMinSizeToLog(long min_class_size_to_log)
	{
		MIN_CLASS_SIZE_TO_LOG = min_class_size_to_log;
	}

	/**
	 * If true deepSizeOf() doesn't compute the final fields of an object.
	 * Default value is false.
	 */
	public static void skipFinalField(boolean skip_final_field)
	{
		SKIP_FINAL_FIELD = skip_final_field;
	}

	/**
	 * If true deepSizeOf() doesn't compute the static fields of an object.
	 * Default value is false.
	 */
	public static void skipStaticField(boolean skip_static_field)
	{
		SKIP_STATIC_FIELD = skip_static_field;
	}

	/**
	 * If true flyweight objects has a size of 0.
	 * Default value is false.
	 */
	public static void skipFlyweightObject(boolean skip)
	{
		SKIP_FLYWEIGHT_FIELD = skip;
	}

	private static void print(String s)
	{
		try
		{
			out.write(s.getBytes());
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static void print(String s, Object... args)
	{
		try
		{
			out.write(String.format(s, args).getBytes());
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Sets the OutputStream to use for logging.
	 * The default OutputStream is System.out
	 * @param o
	 */
	public static void setLogOutputStream(OutputStream o)
	{
		if (o == null)
			throw new IllegalArgumentException("Can't use a null OutputStream");

		out = o;
	}

	/**
	 * Turn on debugging information
	 */
	public static void turnOnDebug()
	{
		debug = true;
	}

	/**
	 * Turn off debugging information
	 */
	public static void turnOffDebug()
	{
		debug = true;
	}
}