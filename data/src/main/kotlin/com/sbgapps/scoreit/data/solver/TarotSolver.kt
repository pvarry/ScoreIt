/*
 * Copyright 2019 Stéphane Baiget
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sbgapps.scoreit.data.solver

import com.sbgapps.scoreit.data.model.*
import kotlin.math.abs

class TarotSolver {

    fun computeResults(lap: TarotLapData): Pair<List<Int>, Boolean> {
        val results = IntArray(lap.playerCount)
        val points = getPoints(lap)

        when (lap.playerCount) {
            3 -> for (player in PLAYER_1..PLAYER_3) results[player] = (if (player == lap.taker) 2 else -1) * points

            4 -> for (player in PLAYER_1..PLAYER_4) results[player] = (if (player == lap.taker) 3 else -1) * points

            5 -> {
                if (lap.taker != lap.partner) {
                    for (player in PLAYER_1..PLAYER_5) {
                        results[player] = when (player) {
                            lap.taker -> 2
                            lap.partner -> 1
                            else -> -1
                        } * points
                    }
                } else {
                    for (player in PLAYER_1..PLAYER_5) results[player] = (if (player == lap.taker) 4 else -1) * points
                }
            }
        }

        return results.toList() to (points >= 0)
    }

    private fun getPoints(lap: TarotLapData): Int {
        var points: Int = when (lap.oudlers) {
            // Only one
            OUDLER_21_MSK, OUDLER_PETIT_MSK, OUDLER_EXCUSE_MSK -> lap.points - POINTS_WITH_ONE_OUDLER
            // Two
            OUDLER_21_MSK or OUDLER_PETIT_MSK,
            OUDLER_21_MSK or OUDLER_EXCUSE_MSK,
            OUDLER_EXCUSE_MSK or OUDLER_PETIT_MSK -> lap.points - POINTS_WITH_TWO_OUDLERS
            // All
            OUDLER_21_MSK or OUDLER_PETIT_MSK or OUDLER_EXCUSE_MSK -> lap.points - POINTS_WITH_THREE_OUDLERS
            // None
            else -> lap.points - POINTS_WITH_NO_OUDLER
        }

        // Contrat
        val isWon = points >= 0
        points = (POINTS_CONTRACT + abs(points)) * lap.bid.coefficient

        // Poignée
        points += getPoigneeBonus(lap)
        points = if (isWon) points else -points

        // Petit au bout
        points += getPetitBonus(lap)

        // Chelem
        points += getChelemBonus(lap)
        return points
    }

    fun computeScores(laps: List<TarotLapData>, playerCount: Int): List<Int> {
        val scores = MutableList(playerCount) { 0 }
        laps.map { computeResults(it).first }.forEach { points ->
            for (player in 0 until playerCount) scores[player] += points[player]
        }
        return scores
    }

    private fun getPetitBonus(lap: TarotLapData): Int {
        for ((player, bonus) in lap.bonuses) {
            if (bonus == TarotBonusData.PETIT_AU_BOUT) {
                when (lap.playerCount) {
                    3, 4 -> return (if (lap.taker == player) 10 else -10) * lap.bid.coefficient
                    5 -> return (if (lap.taker == player || lap.partner == player) 10 else -10) * lap.bid.coefficient
                }
            }
        }
        return 0
    }

    private fun getPoigneeBonus(lap: TarotLapData): Int = lap.bonuses
        .map { it.second }
        .firstOrNull {
            it == TarotBonusData.POIGNEE_SIMPLE || it == TarotBonusData.POIGNEE_DOUBLE || it == TarotBonusData.POIGNEE_TRIPLE
        }?.points ?: 0

    private fun getChelemBonus(lap: TarotLapData): Int = lap.bonuses
        .map { it.second }
        .firstOrNull {
            it == TarotBonusData.CHELEM_NON_ANNONCE || it == TarotBonusData.CHELEM_ANNONCE_REALISE || it == TarotBonusData.CHELEM_ANNONCE_NON_REALISE
        }?.points ?: 0

    fun getAvailableBonuses(lap: TarotLapData): List<TarotBonusData> {
        val currentBonuses = lap.bonuses.map { it.second }
        val bonuses = mutableListOf<TarotBonusData>()
        if (!currentBonuses.contains(TarotBonusData.PETIT_AU_BOUT)) {
            bonuses.add(TarotBonusData.PETIT_AU_BOUT)
        }
        if (!currentBonuses.contains(TarotBonusData.POIGNEE_SIMPLE)
            && !currentBonuses.contains(TarotBonusData.POIGNEE_DOUBLE)
            && !currentBonuses.contains(TarotBonusData.POIGNEE_TRIPLE)
        ) {
            bonuses.add(TarotBonusData.POIGNEE_SIMPLE)
            bonuses.add(TarotBonusData.POIGNEE_DOUBLE)
            bonuses.add(TarotBonusData.POIGNEE_TRIPLE)
        }
        if (!currentBonuses.contains(TarotBonusData.CHELEM_NON_ANNONCE)
            && !currentBonuses.contains(TarotBonusData.CHELEM_ANNONCE_REALISE)
            && !currentBonuses.contains(TarotBonusData.CHELEM_ANNONCE_NON_REALISE)
        ) {
            bonuses.add(TarotBonusData.CHELEM_NON_ANNONCE)
            bonuses.add(TarotBonusData.CHELEM_ANNONCE_REALISE)
            bonuses.add(TarotBonusData.CHELEM_ANNONCE_NON_REALISE)
        }
        return bonuses
    }

    companion object {
        const val OUDLER_NONE = 0
        const val OUDLER_PETIT_MSK = 1
        const val OUDLER_EXCUSE_MSK = 2
        const val OUDLER_21_MSK = 4

        const val POINTS_CONTRACT = 25
        const val POINTS_WITH_NO_OUDLER = 56
        const val POINTS_WITH_ONE_OUDLER = 51
        const val POINTS_WITH_TWO_OUDLERS = 41
        const val POINTS_WITH_THREE_OUDLERS = 36
    }
}