package com.davanok.dvnkquizz.ui.utils.enumStrings

import com.davanok.dvnkquizz.core.domain.enums.QuestionType
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.question_type_auction
import dvnkquizz.sharedui.generated.resources.question_type_cat_in_the_bag
import dvnkquizz.sharedui.generated.resources.question_type_final
import dvnkquizz.sharedui.generated.resources.question_type_normal

val QuestionType.stringRes get() = when(this) {
    QuestionType.NORMAL -> Res.string.question_type_normal
    QuestionType.CAT_IN_THE_BAG -> Res.string.question_type_cat_in_the_bag
    QuestionType.AUCTION -> Res.string.question_type_auction
    QuestionType.FINAL -> Res.string.question_type_final
}