# ![hashmap logo](http://i.imgur.com/OfMyUB8.png)
**This is not necessarily a golfing language.**

    java -jar hashmap.jar -i (.hm file location)

A tutorial will be added later.

## How to program in hashmap

[https://lvivtotoro.github.io/hashmap/](https://lvivtotoro.github.io/hashmap/)

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
