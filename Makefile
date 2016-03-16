test:
		javac HugeInt.java
		javac HugeOps.java
		javac -cp .:junit-4.12.jar HugeOpsTest.java

runTest:
		java -cp .:junit-4.12.jar:hamcrest-core-1.3.jar org.junit.runner.JUnitCore HugeOpsTest

rsa:
		javac HugeInt.java
		javac HugeOps.java
		javac Blocking.java
		javac KeyGen.java
		javac Encrypt.java
		javac RSA.java

run:
		java RSA

enc:
		javac HugeInt.java
		javac HugeOps.java
		javac Encrypt.java

runEnc:
		java Encrypt

block:
		javac Blocking.java

runBlock:
		java Blocking

key:
		javac HugeInt.java
		javac HugeOps.java
		javac KeyGen.java

runKey:
		java KeyGen

clean:
		rm *.class

