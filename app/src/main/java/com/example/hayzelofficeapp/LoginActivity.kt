package com.example.hayzelofficeapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.hayzelofficeapp.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var prefs: SharedPreferences

    companion object {
        private const val TAG = "LoginActivity"
        private const val PREF_NAME = "user_cache"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE)

        // Check if user is already logged in
        if (auth.currentUser != null && prefs.getString("user_name", null) != null) {
            goToDashboard()
            return
        }

        setupListeners()
        styleTextViews()
    }

    private fun setupListeners() {
        // Login button
        binding.loginButton.setOnClickListener {
            login()
        }

        // Forgot password
        binding.forgotPasswordText.setOnClickListener {
            resetPassword()
        }

        // "Don't have an account? Sign Up" text
        binding.signUpText.setOnClickListener {
            showRegisterForm()
        }

        // "Already have an account? Sign In" text
        binding.backToLoginText.setOnClickListener {
            showLoginForm()
        }

        // Register button
        binding.registerButton.setOnClickListener {
            register()
        }

        // Handle enter key in password field
        binding.passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                login()
                true
            } else {
                false
            }
        }
    }

    private fun styleTextViews() {
        try {
            // Style "Don't have an account? Sign Up"
            val signUpText = "Don't have an account? Sign Up"
            val spannableSignUp = android.text.SpannableString(signUpText)

            val signUpStart = signUpText.indexOf("Sign Up")
            val signUpEnd = signUpStart + "Sign Up".length

            if (signUpStart != -1) {
                // Set blue color for "Sign Up"
                spannableSignUp.setSpan(
                    android.text.style.ForegroundColorSpan(
                        ContextCompat.getColor(this, R.color.hayzel_primary)
                    ),
                    signUpStart,
                    signUpEnd,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // Make "Sign Up" bold
                spannableSignUp.setSpan(
                    android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                    signUpStart,
                    signUpEnd,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            binding.signUpText.text = spannableSignUp

            // Style "Already have an account? Sign In"
            val signInText = "Already have an account? Sign In"
            val spannableSignIn = android.text.SpannableString(signInText)

            val signInStart = signInText.indexOf("Sign In")
            val signInEnd = signInStart + "Sign In".length

            if (signInStart != -1) {
                // Set blue color for "Sign In"
                spannableSignIn.setSpan(
                    android.text.style.ForegroundColorSpan(
                        ContextCompat.getColor(this, R.color.hayzel_primary)
                    ),
                    signInStart,
                    signInEnd,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // Make "Sign In" bold
                spannableSignIn.setSpan(
                    android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                    signInStart,
                    signInEnd,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            binding.backToLoginText.text = spannableSignIn
        } catch (e: Exception) {
            Log.e(TAG, "Error styling text views", e)
        }
    }

    private fun showLoginForm() {
        binding.loginForm.visibility = View.VISIBLE
        binding.registerForm.visibility = View.GONE
        binding.loginCard.requestLayout()
        clearRegisterForm()
    }

    private fun showRegisterForm() {
        binding.loginForm.visibility = View.GONE
        binding.registerForm.visibility = View.VISIBLE
        binding.loginCard.requestLayout()
        clearLoginForm()
    }

    private fun clearLoginForm() {
        binding.emailEditText.text?.clear()
        binding.passwordEditText.text?.clear()
        binding.emailEditText.error = null
        binding.passwordEditText.error = null
    }

    private fun clearRegisterForm() {
        binding.nameEditText.text?.clear()
        binding.registerEmailEditText.text?.clear()
        binding.registerPasswordEditText.text?.clear()
        binding.confirmPasswordEditText.text?.clear()
        binding.nameEditText.error = null
        binding.registerEmailEditText.error = null
        binding.registerPasswordEditText.error = null
        binding.confirmPasswordEditText.error = null
        binding.termsCheckbox.isChecked = false
    }

    private fun login() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        // Clear previous errors
        binding.emailEditText.error = null
        binding.passwordEditText.error = null

        // Validation
        if (email.isEmpty()) {
            binding.emailEditText.error = "Email is required"
            binding.emailEditText.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEditText.error = "Please enter a valid email"
            binding.emailEditText.requestFocus()
            return
        }

        if (password.isEmpty()) {
            binding.passwordEditText.error = "Password is required"
            binding.passwordEditText.requestFocus()
            return
        }

        if (password.length < 6) {
            binding.passwordEditText.error = "Password must be at least 6 characters"
            binding.passwordEditText.requestFocus()
            return
        }

        // Show loading on button
        binding.loginButton.isEnabled = false
        binding.loginButton.text = "Signing In..."

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Firebase auth successful")
                    fetchUserAndProceed()
                } else {
                    // Reset button
                    binding.loginButton.isEnabled = true
                    binding.loginButton.text = "Sign In"

                    val error = task.exception
                    val errorMessage = when (error) {
                        is FirebaseAuthInvalidUserException -> "No account found with this email"
                        is FirebaseAuthInvalidCredentialsException -> "Invalid password"
                        else -> "Login failed: ${error?.message ?: "Unknown error"}"
                    }

                    showError(errorMessage)
                    Log.e(TAG, "Login failed", error)
                }
            }
    }

    private fun register() {
        val name = binding.nameEditText.text.toString().trim()
        val email = binding.registerEmailEditText.text.toString().trim()
        val password = binding.registerPasswordEditText.text.toString().trim()
        val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()

        // Clear previous errors
        binding.nameEditText.error = null
        binding.registerEmailEditText.error = null
        binding.registerPasswordEditText.error = null
        binding.confirmPasswordEditText.error = null

        // Validation
        if (name.isEmpty()) {
            binding.nameEditText.error = "Full name is required"
            binding.nameEditText.requestFocus()
            return
        }

        if (email.isEmpty()) {
            binding.registerEmailEditText.error = "Email is required"
            binding.registerEmailEditText.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.registerEmailEditText.error = "Please enter a valid email"
            binding.registerEmailEditText.requestFocus()
            return
        }

        if (password.isEmpty()) {
            binding.registerPasswordEditText.error = "Password is required"
            binding.registerPasswordEditText.requestFocus()
            return
        }

        if (password.length < 6) {
            binding.registerPasswordEditText.error = "Password must be at least 6 characters"
            binding.registerPasswordEditText.requestFocus()
            return
        }

        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordEditText.error = "Please confirm your password"
            binding.confirmPasswordEditText.requestFocus()
            return
        }

        if (password != confirmPassword) {
            binding.confirmPasswordEditText.error = "Passwords do not match"
            binding.confirmPasswordEditText.requestFocus()
            return
        }

        if (!binding.termsCheckbox.isChecked) {
            showError("Please agree to the Terms and Conditions")
            return
        }

        // Show loading on button
        binding.registerButton.isEnabled = false
        binding.registerButton.text = "Creating Account..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        Log.d(TAG, "Firebase auth user created: ${user.uid}")
                        createUserProfile(user.uid, name, email)
                    } else {
                        // Reset button
                        binding.registerButton.isEnabled = true
                        binding.registerButton.text = "Create Account"
                        showError("User creation failed")
                    }
                } else {
                    // Reset button
                    binding.registerButton.isEnabled = true
                    binding.registerButton.text = "Create Account"

                    val error = task.exception
                    val errorMessage = when (error) {
                        is FirebaseAuthUserCollisionException -> "Email already registered"
                        is FirebaseAuthWeakPasswordException -> "Password is too weak"
                        else -> "Registration failed: ${error?.message ?: "Unknown error"}"
                    }

                    showError(errorMessage)
                    Log.e(TAG, "Registration failed", error)
                }
            }
    }

    private fun createUserProfile(uid: String, name: String, email: String) {
        val userData = hashMapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "role" to "employee",
            "createdAt" to com.google.firebase.Timestamp.now(),
            "lastLogin" to com.google.firebase.Timestamp.now()
        )

        firestore.collection("users").document(uid)
            .set(userData)
            .addOnSuccessListener {
                Log.d(TAG, "User profile created in Firestore")

                // Also add to employees collection if needed
                val employeeData = hashMapOf(
                    "uid" to uid,
                    "name" to name,
                    "email" to email,
                    "department" to "General",
                    "position" to "Employee",
                    "joinDate" to com.google.firebase.Timestamp.now()
                )

                firestore.collection("employees").document(uid)
                    .set(employeeData)
                    .addOnSuccessListener {
                        Log.d(TAG, "Employee record created")
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Failed to create employee record", e)
                        // Continue anyway - this is not critical for login
                    }

                // Save user data to shared preferences
                prefs.edit()
                    .putString("user_id", uid)
                    .putString("user_name", name)
                    .putString("user_email", email)
                    .putString("user_role", "employee")
                    .apply()

                showSuccess("Account created successfully!")
                Handler(Looper.getMainLooper()).postDelayed({
                    goToDashboard()
                }, 1500)
            }
            .addOnFailureListener { e ->
                // Reset button
                binding.registerButton.isEnabled = true
                binding.registerButton.text = "Create Account"

                val errorMessage = if (e is FirebaseFirestoreException) {
                    when (e.code) {
                        FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                            "Database access denied. Please check Firestore security rules."
                        }
                        FirebaseFirestoreException.Code.UNAVAILABLE -> {
                            "Network error. Please check your internet connection."
                        }
                        else -> "Failed to create profile: ${e.message}"
                    }
                } else {
                    "Failed to create profile: ${e.message}"
                }

                showError(errorMessage)
                Log.e(TAG, "Failed to create user profile", e)

                // Try to delete the auth user if profile creation failed
                auth.currentUser?.delete()
            }
    }

    private fun fetchUserAndProceed() {
        val uid = auth.currentUser?.uid ?: run {
            binding.loginButton.isEnabled = true
            binding.loginButton.text = "Sign In"
            showError("User not authenticated")
            return
        }

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "User"
                    val email = document.getString("email") ?: ""
                    val role = document.getString("role") ?: "employee"

                    // Update last login time
                    firestore.collection("users").document(uid)
                        .update("lastLogin", com.google.firebase.Timestamp.now())
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Failed to update last login", e)
                        }

                    // Save user data to shared preferences
                    prefs.edit()
                        .putString("user_id", uid)
                        .putString("user_name", name)
                        .putString("user_email", email)
                        .putString("user_role", role)
                        .apply()

                    showSuccess("Welcome back, $name!")
                    Handler(Looper.getMainLooper()).postDelayed({
                        goToDashboard()
                    }, 1000)
                } else {
                    // User document doesn't exist, create it
                    createUserProfile(uid, "User", auth.currentUser?.email ?: "")
                }
            }
            .addOnFailureListener { e ->
                // Reset button
                binding.loginButton.isEnabled = true
                binding.loginButton.text = "Sign In"

                val errorMessage = if (e is FirebaseFirestoreException) {
                    when (e.code) {
                        FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                            "Database access denied. Please check Firestore security rules."
                        }
                        FirebaseFirestoreException.Code.UNAVAILABLE -> {
                            "Network error. Please check your internet connection."
                        }
                        FirebaseFirestoreException.Code.NOT_FOUND -> {
                            "User profile not found"
                        }
                        else -> "Failed to load profile: ${e.message}"
                    }
                } else {
                    "Failed to load profile: ${e.message}"
                }

                showError(errorMessage)
                Log.e(TAG, "Failed to fetch user profile", e)
            }
    }

    private fun resetPassword() {
        val email = binding.emailEditText.text.toString().trim()

        if (email.isEmpty()) {
            showError("Please enter your email address first")
            binding.emailEditText.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Please enter a valid email address")
            binding.emailEditText.requestFocus()
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                showSuccess("Password reset link sent to $email")
            }
            .addOnFailureListener { e ->
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidUserException -> "No account found with this email"
                    else -> "Failed to send reset email: ${e.message}"
                }
                showError(errorMessage)
            }
    }

    private fun showError(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.error_red))
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .show()
    }

    private fun showSuccess(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.success_green))
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .show()
    }

    private fun goToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear any pending handlers
        Handler(Looper.getMainLooper()).removeCallbacksAndMessages(null)
    }
}