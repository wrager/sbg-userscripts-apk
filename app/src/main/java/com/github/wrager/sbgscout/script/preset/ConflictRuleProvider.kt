package com.github.wrager.sbgscout.script.preset

import com.github.wrager.sbgscout.script.model.ScriptConflict
import com.github.wrager.sbgscout.script.model.ScriptIdentifier

interface ConflictRuleProvider {
    fun conflictsFor(identifier: ScriptIdentifier): List<ScriptConflict>
}
