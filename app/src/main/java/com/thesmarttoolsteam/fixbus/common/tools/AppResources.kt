package com.thesmarttoolsteam.fixbus.common

import android.content.Context
import androidx.annotation.*

//--------------------------------------------------------------------------------------------------
/**
 * Récupérer une ressource de type [String]
 *
 * @param context Contexte
 * @param resourceId Identifiant de la ressource
 * @return Ressource de type string
 */
//--------------------------------------------------------------------------------------------------
fun getResString(context: Context?, @StringRes resourceId: Int)
    = context?.getString(resourceId)

//--------------------------------------------------------------------------------------------------
/**
 * Récupérer une ressource de type [String] (avec variables de remplacement)
 *
 * @param context Contexte
 * @param resourceId Identifiant de la ressource
 * @param params Paramètres complémentaires (variables de remplacement)
 * @return Ressource de type string
 */
//--------------------------------------------------------------------------------------------------
fun getResString(context: Context?, @StringRes resourceId: Int, vararg params: Any)
    = context?.getString(resourceId, *params)

//--------------------------------------------------------------------------------------------------
/**
 * Récupérer une ressource de type [String]
 *
 * @param context Contexte
 * @param resourceId Identifiant de la ressource (plural)
 * @param quantity Quantité (pour accord)
 * @param args Paramètres complémentaires (le cas échéant)
 * @return Ressource de type string
 */
//--------------------------------------------------------------------------------------------------
fun getResPlurals(context: Context?, @PluralsRes resourceId: Int, quantity: Int, vararg args: Any)
        = context?.resources?.getQuantityString(resourceId, quantity, *args)

//--------------------------------------------------------------------------------------------------
/**
 * Récupérer une ressource de type [Integer]
 *
 * @param context Contexte
 * @param resourceId Identifiant de l'entier à récupérer
 * @return Valeur définie dans le gradle
 */
//--------------------------------------------------------------------------------------------------
fun getResInteger(context: Context?, @IntegerRes resourceId: Int)
    = context?.resources?.getInteger(resourceId)

//--------------------------------------------------------------------------------------------------
/**
 * Récupérer une ressource de type [Boolean]
 *
 * @param context Contexte
 * @param resourceId Identifiant du booleen à récupérer
 * @return Valeur définie dans le gradle
 */
//--------------------------------------------------------------------------------------------------
fun getResBoolean(context: Context?, @BoolRes resourceId: Int)
    = context?.resources?.getBoolean(resourceId)

//--------------------------------------------------------------------------------------------------
/**
 * Récupérer la valeur d'une couleur à partir de son identifiant [colorId]
 *
 * @param context Contexte
 * @param colorId Identifiant de la couleur
 * @return Code couleur
 */
//--------------------------------------------------------------------------------------------------
fun getResColor(context: Context?, @ColorRes colorId: Int)
    = context?.resources?.getColor(colorId, null)

