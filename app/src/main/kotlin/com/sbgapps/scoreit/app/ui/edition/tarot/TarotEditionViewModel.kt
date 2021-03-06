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

package com.sbgapps.scoreit.app.ui.edition.tarot

import com.sbgapps.scoreit.app.ui.edition.Step
import com.sbgapps.scoreit.core.ui.BaseViewModel
import com.sbgapps.scoreit.data.interactor.GameUseCase
import com.sbgapps.scoreit.data.model.Player
import com.sbgapps.scoreit.data.model.PlayerPosition
import com.sbgapps.scoreit.data.model.TarotBidValue
import com.sbgapps.scoreit.data.model.TarotBonus
import com.sbgapps.scoreit.data.model.TarotBonusValue
import com.sbgapps.scoreit.data.model.TarotLap
import com.sbgapps.scoreit.data.model.TarotOudlerValue
import com.sbgapps.scoreit.data.solver.TarotSolver
import com.sbgapps.scoreit.data.solver.TarotSolver.Companion.POINTS_TOTAL
import io.uniflow.core.flow.UIState

class TarotEditionViewModel(private val useCase: GameUseCase, private val solver: TarotSolver) : BaseViewModel() {

    private val editedLap
        get() = useCase.getEditedLap() as TarotLap

    fun loadContent() {
        setState { getContent() }
    }

    fun setTaker(taker: PlayerPosition) {
        setState {
            useCase.updateEdition(editedLap.copy(taker = taker))
            getContent()
        }
    }

    fun setPartner(partner: PlayerPosition) {
        setState {
            useCase.updateEdition(editedLap.copy(partner = partner))
            getContent()
        }
    }

    fun setBid(bid: TarotBidValue) {
        setState {
            useCase.updateEdition(editedLap.copy(bid = bid))
            getContent()
        }
    }

    fun setOudlers(oudlers: List<TarotOudlerValue>) {
        setState {
            useCase.updateEdition(editedLap.copy(oudlers = oudlers))
            getContent()
        }
    }

    fun incrementScore(increment: Int) {
        setState {
            useCase.updateEdition(editedLap.copy(points = editedLap.points + increment))
            getContent()
        }
    }

    fun addBonus(bonus: Pair<PlayerPosition, TarotBonusValue> /* Player to Bonus */) {
        setState {
            val bonuses = editedLap.bonuses.toMutableList()
            bonuses += TarotBonus(bonus.first, bonus.second)
            useCase.updateEdition(editedLap.copy(bonuses = bonuses))
            getContent()
        }
    }

    fun removeBonus(bonusIndex: Int) {
        setState {
            val bonuses = editedLap.bonuses.toMutableList()
            bonuses.removeAt(bonusIndex)
            useCase.updateEdition(editedLap.copy(bonuses = bonuses))
            getContent()
        }
    }

    fun completeEdition() {
        setState {
            useCase.completeEdition()
            TarotEditionState.Completed
        }
    }

    fun cancelEdition() {
        setState {
            useCase.cancelEdition()
            TarotEditionState.Completed
        }
    }

    private fun getContent(): TarotEditionState.Content {
        return TarotEditionState.Content(
            useCase.getPlayers(),
            editedLap.taker,
            editedLap.partner,
            editedLap.bid,
            editedLap.oudlers,
            editedLap.points,
            editedLap.bonuses.map { it.player to it.bonus },
            solver.getAvailableBonuses(editedLap),
            canStepPointsByOne(editedLap),
            canStepPointsByTen(editedLap)
        )
    }

    private fun canStepPointsByOne(lap: TarotLap): Step = Step(
        (lap.points < POINTS_TOTAL),
        lap.points > 0
    )

    private fun canStepPointsByTen(lap: TarotLap): Step = Step(
        lap.points < (POINTS_TOTAL - 10),
        lap.points > 10
    )
}

sealed class TarotEditionState : UIState() {
    data class Content(
        val players: List<Player>,
        val taker: PlayerPosition,
        val partner: PlayerPosition,
        val bid: TarotBidValue,
        val oudlers: List<TarotOudlerValue>,
        val points: Int,
        val selectedBonuses: List<Pair<PlayerPosition, TarotBonusValue>>,
        val availableBonuses: List<TarotBonusValue>,
        val stepPointsByOne: Step,
        val stepPointsByTen: Step
    ) : TarotEditionState()

    object Completed : TarotEditionState()
}
