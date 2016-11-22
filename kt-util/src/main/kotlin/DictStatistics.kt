package com.mmm.his.nlp.rapidcontent.util

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import java.nio.charset.Charset
import javax.xml.parsers.SAXParserFactory


val variantStrSet : MutableSet<String> = mutableSetOf()
val propertyMap : MutableMap<String, Integer> = mutableMapOf()
val propertyValueSet :MutableSet<String> = mutableSetOf()
var propertySn = Integer(1)

fun main(args: Array<String>) {
    val handler = DictStatiscisHandler()
    val factory = SAXParserFactory.newInstance()
    val parser = factory.newSAXParser()
    val list = File("/Users/a4d98zz/tmp/tmp/com/mmm/his/nlp/dictionary/resources")
            .listFiles()
            .filter { it.name.endsWith(".xml") }
            .toList()
    for (file in list) {
        println("parsing " + file.name + " ...")
        parser.parse(file, handler)
    }

    val varFile = File("/Users/a4d98zz/tmp/varstr.txt").printWriter(Charset.forName("UTF-8"))
    val propertyFile = File("/Users/a4d98zz/tmp/prop.txt").printWriter(Charset.forName("UTF-8"))
    val propValueFile = File("/Users/a4d98zz/tmp/propValue.txt").printWriter(Charset.forName("UTF-8"))

    for(str in variantStrSet){
        varFile.println(str)
    }

    for((key,value) in propertyMap){
        propertyFile.println("$value\t$key")
    }

    for(str in propertyValueSet){
        propValueFile.println(str)
    }

    varFile.close()
    propertyFile.close()
    propValueFile.close()

}


class DictStatiscisHandler : DefaultHandler() {

    override fun startElement(url: String, localName: String, fullName: String, attrs: Attributes) {
        when (fullName) {
            "token" -> {
                val len = attrs.length
                for(i in 0..len-1) {
                    val aname = attrs.getQName(i)
                    val avalue = attrs.getValue(i)

                    var pid = propertyMap.get(aname)
                    if(pid == null){
                        pid = propertySn
                        propertyMap.put(aname, propertySn)
                        propertySn = Integer(propertySn.toInt()+1)
                    }

                    propertyValueSet.add(avalue+"\t"+pid)
                }

            }
            "variant" -> {
                val varStr = attrs.getValue("base")
                variantStrSet.add(varStr.replace("\\","\\\\"))
            }
        }
    }
}

