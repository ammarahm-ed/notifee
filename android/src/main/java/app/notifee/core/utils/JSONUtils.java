package app.notifee.core.utils;

import android.os.Build;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

// TODO Flipper: improve & cleanup, nested not working?
public class JSONUtils {
  public static Bundle convertToBundle(JSONObject jsonObject) {
    Bundle bundle = new Bundle();
    Iterator<String> jsonIterator = jsonObject.keys();
    while (jsonIterator.hasNext()) {
      try {
        String key = jsonIterator.next();
        Object value = jsonObject.get(key);
        if (value == JSONObject.NULL) {
          continue;
        }

        if (value instanceof JSONObject) {
          bundle.putBundle(key, convertToBundle((JSONObject) value));
          continue;
        }

        Class clazz = value.getClass();

        if (clazz == JSONArray.class) {
          setJSONArray(bundle, key, (JSONArray) value);
        } else if (clazz == String.class) {
          bundle.putString(key, (String) value);
        } else if (clazz == Boolean.class) {
          bundle.putBoolean(key, (Boolean) value);
        } else if (clazz == boolean.class) {
          bundle.putBoolean(key, (boolean) value);
        } else if (clazz == Integer.class) {
          bundle.putInt(key, (Integer) value);
        } else if (clazz == int.class) {
          bundle.putInt(key, (int) value);
        } else if (clazz == Long.class) {
          bundle.putLong(key, (Long) value);
        } else if (clazz == long.class) {
          bundle.putLong(key, (long) value);
        } else if (clazz == Double.class) {
          bundle.putDouble(key, (Double) value);
        } else if (clazz == double.class) {
          bundle.putDouble(key, (double) value);
        } else {
          throw new IllegalArgumentException("Unexpected type: " + clazz);
        }
      } catch (JSONException e) {
        // ignore exceptions
      }
    }

    return bundle;
  }

  @SuppressWarnings("unchecked")
  private static Object convertToJavaArray(Class clazz, JSONArray jsonArray) throws JSONException {
    Object array = Array.newInstance(clazz, jsonArray.length());
    for (int i = 0; i < jsonArray.length(); i++) {
      Object current = jsonArray.get(i);
      if (clazz.isAssignableFrom(unboxClass(current.getClass()))) {
        Array.set(array, i, current);
      } else {
        throw new IllegalArgumentException("Unexpected type in an array: " + current.getClass() +
          ". All array elements must be same type.");
      }
    }

    return array;
  }

  private static Class unboxClass(Class clazz) {
    if (clazz == Byte.class) {
      return byte.class;
    } else if (clazz == Character.class) {
      return char.class;
    } else if (clazz == Float.class) {
      return float.class;
    } else if (clazz == Short.class) {
      return short.class;
    } else if (clazz == Boolean.class) {
      return boolean.class;
    } else if (clazz == Double.class) {
      return double.class;
    } else if (clazz == Integer.class) {
      return int.class;
    } else if (clazz == Long.class) {
      return long.class;
    } else {
      return clazz;
    }
  }

  private static void setJSONArray(Bundle bundle, String key, JSONArray jsonArray) throws
    JSONException {
    // Empty list, can't even figure out the type, assume an ArrayList<String>
    if (jsonArray.length() == 0) {
      bundle.putStringArray(key, new String[0]);
    } else {
      Object first = jsonArray.get(0);

      // We could do this all with reflection but this seems (slightly) better maybe?
      Class clazz = unboxClass(first.getClass());
      if (byte.class.isAssignableFrom(clazz)) {
        bundle.putByteArray(key, (byte[]) convertToJavaArray(byte.class, jsonArray));
      } else if (char.class.isAssignableFrom(clazz)) {
        bundle.putCharArray(key, (char[]) convertToJavaArray(char.class, jsonArray));
      } else if (float.class.isAssignableFrom(clazz)) {
        bundle.putFloatArray(key, (float[]) convertToJavaArray(float.class, jsonArray));
      } else if (short.class.isAssignableFrom(clazz)) {
        bundle.putShortArray(key, (short[]) convertToJavaArray(short.class, jsonArray));
      } else if (String.class.isAssignableFrom(clazz)) {
        bundle.putStringArray(key, (String[]) convertToJavaArray(String.class, jsonArray));
      } else if (boolean.class.isAssignableFrom(clazz)) {
        bundle.putBooleanArray(key, (boolean[]) convertToJavaArray(boolean.class, jsonArray));
      } else if (double.class.isAssignableFrom(clazz)) {
        bundle.putDoubleArray(key, (double[]) convertToJavaArray(double.class, jsonArray));
      } else if (int.class.isAssignableFrom(clazz)) {
        bundle.putIntArray(key, (int[]) convertToJavaArray(int.class, jsonArray));
      } else if (long.class.isAssignableFrom(clazz)) {
        bundle.putLongArray(key, (long[]) convertToJavaArray(long.class, jsonArray));
      } else if (first instanceof JSONObject) {
        ArrayList<Bundle> parcelableArrayList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
          Object current = jsonArray.get(i);
          if (current instanceof JSONObject) {
            parcelableArrayList.add(convertToBundle((JSONObject) current));
          } else {
            throw new IllegalArgumentException(
              "Unexpected type in an array: " + current.getClass());
          }
        }

        Bundle[] array = new Bundle[parcelableArrayList.size()];
        array = parcelableArrayList.toArray(array);
        bundle.putParcelableArray(key, array);
      } else {
        throw new IllegalArgumentException("Unexpected type in an array: " + first.getClass());
      }
    }
  }

  public static JSONObject convertToJSON(Bundle bundle) throws JSONException {
    JSONObject jsonObject = new JSONObject();
    Iterator<String> jsonIterator = bundle.keySet().iterator();

    while (jsonIterator.hasNext()) {
      String key = jsonIterator.next();
      jsonObject.put(key, jsonWrapValue(bundle.get(key)));
    }

    return jsonObject;
  }

  private static Object jsonWrapValue(Object valueObject) {
    if (valueObject == null) {
      return null;
    } else {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        return JSONObject.wrap(valueObject);
      } else {
        return null;
      }
    }
  }
}
