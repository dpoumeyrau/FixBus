package com.thesmarttoolsteam.fixbus.common.services

import android.content.Context
import com.thesmarttoolsteam.fixbus.R
import com.thesmarttoolsteam.fixbus.common.TIMBER_BLANKCHAR
import com.thesmarttoolsteam.fixbus.common.TIMBER_MAXLENGTH
import com.thesmarttoolsteam.fixbus.common.getResBoolean
import com.thesmarttoolsteam.fixbus.common.getResString
import timber.log.Timber

/**
 * Gestion des logs en fonction du type de build
 */
object AppTimber {

    private lateinit var timberLogTag: String

    /**
     * Initialisation de la classe [Timber]
     *
     */
    fun initAppTimber(applicationContext: Context) {

        val isTimberEnabled = getResBoolean(
            applicationContext,
            R.bool.gradle_configTimberEnabled
        ) ?: false
        timberLogTag = getResString(applicationContext, R.string.gradle_configTimberTag) ?: ""


        if (isTimberEnabled) {
            Timber.plant(AppTimberDebugTree())
            Timber.i("Activation de Timber en mode Debug " +
                    "(gradle_configTimberEnabled = true)")
        } else {
            Timber.plant(AppTimberReleaseTree())
            Timber.i("Activation de Timber en mode Release " +
                    "(gradle_configTimberEnabled = false)")
        }
    }

    /**
     * Gestion de [Timber] en mode Debug
     */
    class AppTimberDebugTree : Timber.DebugTree() {
        /**
         * Extension de la méthode [Timber.DebugTree.createStackElementTag]
         * La méthode construit le tag de la trace Timber en concaténant le tag de l'application,
         * le nom du fichier, le numéro de ligne et le nom de la méthode.
         *
         * @param element Données de trace
         * @return Tag de la trace Timber
         */
        override fun createStackElementTag(element: StackTraceElement): String {
            var tag = "$timberLogTag (${element.fileName}:${element.lineNumber}) " +
                    "${element.methodName}()$TIMBER_BLANKCHAR"
            if (tag.length > TIMBER_MAXLENGTH)
                tag = tag.substring(0, TIMBER_MAXLENGTH - 8) + "...()$TIMBER_BLANKCHAR"
            return tag
        }
    }

    /**
     * Gestion de [Timber] en mode Release
     *
     * Aucune gestion des logs ==> Les appels seront supprimés par ProGuard.
     */
    class AppTimberReleaseTree : Timber.Tree() {
        /**
         * Extension de la méthode [Timber.Tree.log]
         * La méthode est surchargée et ne retourne aucune trace
         * Les appels seront alors supprimés par ProGuard (si activé)
         */
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {}
    }
}