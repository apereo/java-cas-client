CENTRAL AUTHENTICATION SERVICE (CAS)
--------------------------------------------------------------------
http://www.ja-sig.org/products/cas/

1.  INTRODUCTION

The Central Authentication Service (CAS) is the standard mechanism by which web
applications should authenticate users. Any custom applications written benefit
from using CAS.

Note that CAS provides authentication; that is, it determines that your users
are who they say they are. CAS should not be viewed as an access-control system;
in particular, providers of applications that grant access to anyone who
possesses a NetID should understand that loose affiliates of an organization may
be granted NetIDs.

The JA-SIG CAS Client for Java is a support library for Java applications to communicate
with the CAS server.

2.  RELEASE INFO

CAS requires J2SE 1.4 and J2EE1.3.

Release conents:
* "src/main/java" contains the Java source files for the framework
* "src/test/java" contains the Java source files for CAS's test suite


