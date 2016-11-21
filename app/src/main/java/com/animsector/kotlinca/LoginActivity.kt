package com.animsector.kotlinca

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Patterns
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity() {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    lateinit private var mAuthTask: UserLoginTask


    // VALIDATION EXPRESSION FUNCTIONS
    val empty: (String) -> Boolean = { it.isEmpty() }
    val isPassword: (String) -> Boolean = { it.length > 4 }
    val isEmail: (String) -> Boolean = { Patterns.EMAIL_ADDRESS.matcher(it).matches() }

    fun EditText.validateWith(func: (String) -> Boolean): Boolean {
        return func(text.toString())
    }

    // UI references.
    @BindView(R.id.email) lateinit var emailInput: AutoCompleteTextView
    @BindView(R.id.password) lateinit var passwordInput: EditText
    @BindView(R.id.login_progress) lateinit var progressView: View
    @BindView(R.id.login_form) lateinit var loginForm: View

    @Suppress("unused")
    @OnClick(R.id.email_sign_in_button)
    fun onSignInButtonClicked() {
        attemptLogin()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ButterKnife.bind(this)

        passwordInput.setOnEditorActionListener(TextView.OnEditorActionListener { textView, id, keyEvent ->
            if (id == R.id.login || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {

        // Reset errors.
        emailInput.error = null
        passwordInput.error = null

        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (passwordInput.validateWith(empty)) {
            passwordInput.error = getString(R.string.error_field_required)
            focusView = passwordInput
        } else if (!passwordInput.validateWith(isPassword)) {
            passwordInput.error = getString(R.string.error_invalid_password)
            focusView = passwordInput
        }

        // Check for a valid email address.
        if (emailInput.validateWith(empty)) {
            emailInput.error = getString(R.string.error_field_required)
            focusView = emailInput

        } else if (!emailInput.validateWith(isEmail)) {
            emailInput.error = getString(R.string.error_invalid_email)
            focusView = emailInput

        }

        if (focusView != null) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)
            mAuthTask = UserLoginTask(this, emailInput.text.toString(), passwordInput.text.toString())
            mAuthTask.execute()
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)

            loginForm.visibility = if (show) View.GONE else View.VISIBLE
            loginForm.animate().setDuration(shortAnimTime.toLong()).alpha(
                    (if (show) 0 else 1).toFloat()).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    loginForm.visibility = if (show) View.GONE else View.VISIBLE
                }
            })

            progressView.visibility = if (show) View.VISIBLE else View.GONE
            progressView.animate().setDuration(shortAnimTime.toLong()).alpha(
                    (if (show) 1 else 0).toFloat()).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    progressView.visibility = if (show) View.VISIBLE else View.GONE
                }
            })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.visibility = if (show) View.VISIBLE else View.GONE
            loginForm.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    inner class UserLoginTask internal constructor(private val context: Context, private val mEmail: String, private val mPassword: String) : AsyncTask<Void, Void, LoginResult>() {

        // Simulation of Network operation
        override fun doInBackground(vararg params: Void): LoginResult {

            try {
                Thread.sleep(2000)
            } catch (e: InterruptedException) {
                return LoginResult.UNKNOWN_ERROR
            }

            DUMMY_CREDENTIALS
                    .map { it.split(":") }
                    .filter { it[0] == mEmail }
                    .forEach {
                        if (it[1] == mPassword) {
                            return LoginResult.SUCCESSFUL
                        } else {
                            return LoginResult.INVALID_PASSWORD
                        }
                    }

            return LoginResult.INVALID_EMAIL
        }

        override fun onPostExecute(loginResult: LoginResult) {
            showProgress(false)

            if (loginResult == LoginResult.INVALID_EMAIL) {
                emailInput.error = getString(R.string.error_invalid_email)
                emailInput.requestFocus()
            } else if (loginResult == LoginResult.INVALID_PASSWORD) {
                passwordInput.error = getString(R.string.error_incorrect_password)
                passwordInput.requestFocus()
            } else if(loginResult == LoginResult.UNKNOWN_ERROR) {
                Toast.makeText(context, "Unknown error", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Logged in", Toast.LENGTH_LONG).show()
            }
        }

        override fun onCancelled() {
            showProgress(false)
        }
    }

    enum class LoginResult {
        SUCCESSFUL, INVALID_PASSWORD, INVALID_EMAIL, UNKNOWN_ERROR
    }

    companion object {

        /**
         * A dummy authentication store containing known user names and passwords.
         * TODO: remove after connecting to a real authentication system.
         */
        private val DUMMY_CREDENTIALS = arrayOf("foo@example.com:hello", "bar@example.com:world")
    }
}

