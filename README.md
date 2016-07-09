# ![hashmap logo](http://i.imgur.com/OfMyUB8.png)
**This is not necessarily a golfing language.**

    java -jar hashmap.jar -i (.hm file location)

A tutorial will be added later.

## How to program in hashmap

[https://lvivtotoro.github.io/hashmap/](https://lvivtotoro.github.io/hashmap/)

## How to parse hashmap in Java
After v4, developers can parse hashmap code in their Java programs.

```java
Hashmap hashmap = new Hashmap("your hashmap code here", workingFile);
hashmap.registerDefaultFunctions();
hashmap.interpret(0, false);
```
The `workingFile` field is the file where the hashmap code is at.  
`hashmap.registerDefaultFunctions();` adds the default builtin functions such as the `io` library, so it's optional.  
`hashmap.interpret(0, false);` Starts interpreting the hashmap code from the start. The `false` specifies that the interpretation should not loop (to allow while statements).

## Simple programs
You may add new programs here with pull requests.

### Simple decoding program (by Midnightas, or lvivtotoro)

    {
    	""
    	{;ad;"key"/c+}$'af
    }:"decode" >>> This is the decoding function
    {
    	""
    	{;ad;"key"*c+}$'af
    }:"encode" >>> Encoding function
    "Please type in the key:"|. >>> For more security
    h:"key"
    "Type encode for encoding, decode for decoding."|.
    i:"input"
    "Please type in your sentence:"|.
    {i;"encode">|.!};"input""encode"=?
    {i;"decode">|.!};"input""decode"=?
