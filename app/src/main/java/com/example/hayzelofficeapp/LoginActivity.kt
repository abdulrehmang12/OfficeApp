package com.example.hayzelofficeapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.hayzelofficeapp.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        prefs = getSharedPreferences("user_cache", MODE_PRIVATE)

        // Check if user is already logged in
        if (auth.currentUser != null && prefs.getString("user_name", null) != null) {
            goToDashboard()
            return
        }

        setupListeners()
        styleTextViews() // Add this line to style the text views
    }

    private fun setupListeners() {
        // Login button
        binding.loginButton.setOnClickListener { login() }

        // Forgot password
        binding.forgotPasswordText.setOnClickListener { resetPassword() }

        // "Don't have an account? Sign Up" text
        binding.signUpText.setOnClickListener {
            showRegisterForm()
        }

        // "Already have an account? Sign In" text
        binding.backToLoginText.setOnClickListener {
            showLoginForm()
        }

        // Register button
        binding.registerButton.setOnClickListener { register() }
    }

    private fun styleTextViews() {
        // Style "Don't have an account? Sign Up" - make only "Sign Up" blue
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

            // Make "Sign Up" bold (optional)
            spannableSignUp.setSpan(
                android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                signUpStart,
                signUpEnd,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        binding.signUpText.text = spannableSignUp

        // Style "Already have an account? Sign In" - make only "Sign In" blue
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

            // Make "Sign In" bold (optional)
            spannableSignIn.setSpan(
                android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                signInStart,
                signInEnd,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        binding.backToLoginText.text = spannableSignIn
    }

    private fun showLoginForm() {
        binding.loginForm.visibility = View.VISIBLE
        binding.registerForm.visibility = View.GONE
        binding.loginCard.requestLayout() // Force layout update
    }

    private fun showRegisterForm() {
        binding.loginForm.visibility = View.GONE
        binding.registerForm.visibility = View.VISIBLE
        binding.loginCard.requestLayout() // Force layout update
    }

    private fun login() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        // Validation
        if (email.isEmpty()) {
            binding.emailEditText.error = "Email is required"
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

        binding.loginButton.isEnabled = false
        binding.loginButton.text = "Signing In..."

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                fetchUserAndProceed()
            }
            .addOnFailureListener { e ->
                binding.loginButton.isEnabled = true
                binding.loginButton.text = "Sign In"
                showError("Login failed: ${e.message}")
            }
    }

    private fun register() {
        val name = binding.nameEditText.text.toString().trim()
        val email = binding.registerEmailEditText.text.toString().trim()
        val password = binding.registerPasswordEditText.text.toString().trim()
        val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()

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

        binding.registerButton.isEnabled = false
        binding.registerButton.text = "Creating Account..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    // Create user profile in Firestore
                    val userData = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "role" to "employee", // Default role
                        "createdAt" to com.google.firebase.Timestamp.now()
                    )

                    firestore.collection("users").document(user.uid)
                        .set(userData)
                        .addOnSuccessListener {
                            // Save user data to shared preferences
                            prefs.edit()
                                .putString("user_id", user.uid)
                                .putString("user_name", name)
                                .putString("user_role", "employee")
                                .apply()

                            showSuccess("Account created successfully!")
                            Handler(Looper.getMainLooper()).postDelayed({
                                goToDashboard()
                            }, 1000)
                        }
                        .addOnFailureListener { e ->
                            binding.registerButton.isEnabled = true
                            binding.registerButton.text = "Create Account"
                            showError("Failed to create profile: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                binding.registerButton.isEnabled = true
                binding.registerButton.text = "Create Account"
                showError("Registration failed: ${e.message}")
            }
    }

    private fun fetchUserAndProceed() {
        val uid = auth.currentUser?.uid ?: run {
            binding.loginButton.isEnabled = true
            binding.loginButton.text = "Sign In"
            return
        }

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    binding.loginButton.isEnabled = true
                    binding.loginButton.text = "Sign In"
                    showError("User profile not found")
                    return@addOnSuccessListener
                }

                val name = doc.getString("name") ?: "User"
                val role = doc.getString("role") ?: "employee"

                // Save user data to shared preferences
                prefs.edit()
                    .putString("user_id", uid)
                    .putString("user_name", name)
                    .putString("user_role", role)
                    .apply()

                showSuccess("Welcome back, $name!")
                Handler(Looper.getMainLooper()).postDelayed({
                    goToDashboard()
                }, 700)
            }
            .addOnFailureListener {
                binding.loginButton.isEnabled = true
                binding.loginButton.text = "Sign In"
                showError("Failed to load profile")
            }
    }

    private fun resetPassword() {
        val email = binding.emailEditText.text.toString().trim()

        if (email.isEmpty()) {
            showError("Please enter your email address first")
            binding.emailEditText.requestFocus()
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                showSuccess("Password reset link sent to your email")
            }
            .addOnFailureListener {
                showError("Failed to send reset email")
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
}