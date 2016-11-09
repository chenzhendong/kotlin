package com.mmm.his.nlp.rapidcontent.util

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import javax.xml.parsers.SAXParserFactory


val varStrSet : MutableSet<String> = mutableSetOf()
val propValueSets : MutableMap<String, MutableSet<String>> = mutableMapOf()


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


    println("var string size: ${varStrSet.size}")
    for( propName in propValueSets.keys){
        println("$propName: ${propValueSets[propName]?.size}")
    }
    /*val varFile = File("/Users/a4d98zz/tmp/varstr.txt").printWriter(Charset.forName("UTF-8"))
    val cuiFile = File("/Users/a4d98zz/tmp/cuistr.txt").printWriter(Charset.forName("UTF-8"))

    for(str in varStrSet){
        varFile.println(str)
    }

    for(str in cuiSet){
        cuiFile.println(str)
    }*/

}


class DictStatiscisHandler : DefaultHandler() {

    override fun startElement(url: String, localName: String, fullName: String, attrs: Attributes) {
        when (fullName) {
            "token" -> {
                val len = attrs.length
                for(i in 0..len-1) {
                    val aname = attrs.getQName(i)
                    val avalue = attrs.getValue(i)
                    val pset = propValueSets.get(aname)
                    if(pset == null){
                        val newSet = mutableSetOf(avalue)
                        propValueSets.put(aname, newSet)
                    } else {
                        pset.add(avalue)
                    }

                }

            }
            "variant" -> {
                val varStr = attrs.getValue("base")
                varStrSet.add(varStr)
            }
        }
    }
}

