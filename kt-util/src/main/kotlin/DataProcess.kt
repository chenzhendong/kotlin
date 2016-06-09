package com.popyoyo.kotlin.ktutil
/**
 * Created by a4d98zz on 6/9/16.
 */

import java.io.File

fun main(args: Array<String>) {
    if (args.size < 3 ) {
        println("Please provide input file names: 1)finalCodeEvaluation.csv 2) enc-doc-mapping.csv 3) encId-code.csv  ")
        return
    }

    val (conMap, docMachineCodeMap) = buildConsolidatedDataStructures(args[0])
    val docEncMap = buildDocEncMap(args[1])
    val encHumanCodeMap = buildEncHumanCodeMap(args[2])

    for (entry in docMachineCodeMap) {
        val docId = entry.key
        val machineCodes = docMachineCodeMap.get(docId)
        val encId = docEncMap.get(docId)
        val humanCodes = encHumanCodeMap.get(encId)


        for (mcode in machineCodes!!) {
            val rec = conMap.get(DocCodePair(docId, mcode))

            if (humanCodes != null && humanCodes.contains(mcode)) {
                rec?.autoAssigned = 1
                rec?.humanAssigned = 1
                rec?.status = "correct"
            } else {
                rec?.autoAssigned = 1
                rec?.humanAssigned = 0
                rec?.status = "incorrect"
            }
        }

        if (humanCodes != null) {
            for (hcode in humanCodes) {
                if (!machineCodes.contains(hcode)) {
                    val rec = ConsolidatedRecord(docId, "", "",
                            hcode, 0, 1, "missing")
                    conMap.put(DocCodePair(docId, hcode), rec)
                }
            }
        }

    }
    File("hca_consolidated.csv").printWriter().use { out ->
        conMap.forEach {
            val rec = it.value
            out.println("${rec.docId},${rec.familyNcid},${rec.roleNcid},${rec.code},${rec.autoAssigned},${rec.humanAssigned},${rec.status}")
        }
        println("${conMap.size} lines are written to hca_consolidated.csv.")
    }
}

data class ConsolidatedRecord(val docId: String, val familyNcid: String,
                              val roleNcid: String, val code: String,
                              var autoAssigned: Int, var humanAssigned: Int,
                              var status: String)

data class DocCodePair(val docId: String, val code: String)

fun buildConsolidatedDataStructures(fileName: String): Pair<MutableMap<DocCodePair, ConsolidatedRecord>, MutableMap<String, MutableSet<String>>> {
    val list = File(fileName).readLines().toMutableList()
    if (list.get(0).startsWith("note")) list.removeAt(0)

    val conMap = mutableMapOf<DocCodePair, ConsolidatedRecord>()
    val docCodeMap = mutableMapOf<String, MutableSet<String>>()

    for (line in list) {
        val arr = line.split(",")
        val key = DocCodePair(arr[0], arr[3])
        val value = ConsolidatedRecord(
                arr[0], arr[1], arr[2], arr[3],
                arr[4].toInt(), arr[5].toInt(), arr[6]
        )
        conMap.put(key, value)
        val set = docCodeMap.get(arr[0]) ?: mutableSetOf<String>()
        set.add(arr[3])
        docCodeMap.put(arr[0], set)
    }

    println("${list.size} lines are loaded from ${fileName}.")
    println("${docCodeMap.size} documents are found.")
    return Pair(conMap, docCodeMap)
}

fun buildDocEncMap(fileName: String): MutableMap<String, String> {
    val list = File(fileName).readLines()
    val map = mutableMapOf<String, String>()
    for (line in list) {
        val arr = line.split(",")
        map.put(arr[1], arr[0])
    }
    println("${list.size} lines are loaded from ${fileName}.")
    return map
}

fun buildEncHumanCodeMap(fileName: String): MutableMap<String, MutableSet<String>> {
    val list = File(fileName).readLines()
    val map = mutableMapOf<String, MutableSet<String>>()
    for (line in list) {
        val arr = line.split(",")
        val set = map.get(arr[0]) ?: mutableSetOf<String>()
        set.add(arr[1])
        map.put(arr[0], set)
    }
    println("${list.size} lines are loaded from ${fileName}.")
    println("${map.size} encouters are found.")
    return map
}