package org.spideruci.analysis.dynamic;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class RuntimeClassRedefiner implements ClassFileTransformer {

  @Override
  public byte[] transform(ClassLoader loader, String className,
      Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
      byte[] classfileBuffer) throws IllegalClassFormatException {
    boolean tempGuard = Profiler.$guard1$; 
    Profiler.$guard1$ = true;

    if(!RedefinitionTargets.isTarget(className)) {
      Profiler.$guard1$ = tempGuard;
      return null;
    }

    if(RedefinitionTargets.isException(className)) {
      Profiler.$guard1$ = tempGuard;
      return null;
    }

    byte[] instrumented_bytes = Blinksformer.instrumentClass(className, 
        classfileBuffer, true /*isRuntime*/);

    Profiler.$guard1$ = tempGuard;
    return instrumented_bytes;
  }

  public static class RedefinitionTargets {
    private static final String[] wildCardTargets = new String[] {
        "java/util",
//        "java/net/URLClassLoader", 
//        "java/security/SecureClassLoader",
//        "java/lang/ClassLoader"
//        "java/util/concurrent"
        "java/security"
    };

    private static final String[] exactTargets = new String[] {
//        "java/util/Hashtable", "java/util/regex/Pattern",  "java/util/ArrayList"
        "java/net/URLClassLoader", "java/security/SecureClassLoader"
    };

    public static boolean isTarget(String className) {
      for(String wildCard : wildCardTargets) {
        if(className.startsWith(wildCard)) {
          return true;
        }
      }

      return false;
    }

    public static boolean isExactTarget(String className) {
      for(String exactTarget : exactTargets) {
        if(className.equals(exactTarget)) {
          return true;
        }
      }
      return false;
    }

    private static final String[] wildCardExceptions = new String[] {
//        "java/security", 
//        "java/util/regex"
        "java/security/AccessControl"
    };

    private static final String[] exactExceptions = new String[] {
//        "java/util/Hashtable", "java/util/regex/Pattern", "java/util/regex/Matcher"
        "java/net/URLClassLoader", "java/security/SecureClassLoader"
    };

    public static boolean isException(String className) {
      for(String wildCard : wildCardExceptions) {
        if(className.startsWith(wildCard)) {
          return true;
        }
      }

      return false;
    }

    public static boolean isExactException(String className) {
      for(String exactTarget : exactExceptions) {
        if(className.equals(exactTarget)) {
          return true;
        }
      }
      return false;
    }

  }

}

