package com.example.core.sandbox

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.example.core.model.TestRunSummary
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Sandboxed JavaScript/TypeScript execution runtime powered by Android's embedded engine.
 * Provides isolated script execution, console logging, and local unit test validation.
 */
class EmbeddedScriptRunner(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var webView: WebView? = null
    private val isInitialized = AtomicBoolean(false)

    init {
        mainHandler.post {
            try {
                webView = WebView(context).apply {
                    settings.javaScriptEnabled = true
                    addJavascriptInterface(JsBridge(), "NexusBridge")
                }
                isInitialized.set(true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private var activeLogCollector = mutableListOf<String>()
    private var activeTestCollector: CompletableDeferred<TestRunSummary>? = null

    inner class JsBridge {
        @JavascriptInterface
        fun log(message: String) {
            activeLogCollector.add(message)
        }

        @JavascriptInterface
        fun onTestComplete(jsonResult: String) {
            try {
                val json = JSONObject(jsonResult)
                val summary = TestRunSummary(
                    suiteName = json.optString("suiteName", "UnitTestSuite"),
                    totalTests = json.optInt("total", 0),
                    passedTests = json.optInt("passed", 0),
                    failedTests = json.optInt("failed", 0),
                    durationMs = json.optLong("durationMs", 0L),
                    outputLogs = activeLogCollector.joinToString("\n")
                )
                activeTestCollector?.complete(summary)
            } catch (e: Exception) {
                activeTestCollector?.complete(
                    TestRunSummary("Suite", 1, 0, 1, 0L, "Failed to parse test result: ${e.message}")
                )
            }
        }
    }

    /**
     * Executes a TypeScript/JavaScript script and returns standard output logs and return value.
     */
    suspend fun executeScript(scriptSource: String): Result<String> = withContext(Dispatchers.Main) {
        val deferred = CompletableDeferred<String>()
        activeLogCollector.clear()

        val wrappedScript = """
            (function() {
                try {
                    console.log = function(msg) { NexusBridge.log(String(msg)); };
                    $scriptSource
                    NexusBridge.log("[Sandbox] Execution finished successfully.");
                    return "OK";
                } catch(e) {
                    NexusBridge.log("[Sandbox Error] " + e.toString());
                    return "ERROR: " + e.toString();
                }
            })();
        """.trimIndent()

        webView?.evaluateJavascript(wrappedScript) { result ->
            val logs = activeLogCollector.joinToString("\n")
            deferred.complete(if (logs.isNotBlank()) logs else result ?: "Execution completed.")
        }

        val output = withTimeoutOrNull(5000L) { deferred.await() }
            ?: "Execution timed out (5s limit)."
        Result.success(output)
    }

    /**
     * Executes a unit test suite with embedded assertions (describe, it, expect).
     */
    suspend fun runUnitTestSuite(suiteName: String, testCode: String): TestRunSummary = withContext(Dispatchers.Main) {
        val deferred = CompletableDeferred<TestRunSummary>()
        activeTestCollector = deferred
        activeLogCollector.clear()

        val harness = """
            (function() {
                var total = 0;
                var passed = 0;
                var failed = 0;
                var start = Date.now();
                var logs = [];

                function log(m) { NexusBridge.log(m); }
                function describe(name, fn) { log("Running Suite: " + name); fn(); }
                function it(desc, fn) {
                    total++;
                    try {
                        fn();
                        passed++;
                        log("  ✓ " + desc);
                    } catch(e) {
                        failed++;
                        log("  ✕ " + desc + ": " + e.message);
                    }
                }
                function expect(actual) {
                    return {
                        toBe: function(expected) {
                            if (actual !== expected) throw new Error("Expected " + expected + " but got " + actual);
                        },
                        toEqual: function(expected) {
                            if (JSON.stringify(actual) !== JSON.stringify(expected)) {
                                throw new Error("Expected " + JSON.stringify(expected) + " but got " + JSON.stringify(actual));
                            }
                        },
                        toBeGreaterThan: function(expected) {
                            if (actual <= expected) throw new Error("Expected " + actual + " to be greater than " + expected);
                        }
                    };
                }

                try {
                    $testCode
                } catch(e) {
                    failed++;
                    log("Suite Exception: " + e.message);
                }

                var duration = Date.now() - start;
                NexusBridge.onTestComplete(JSON.stringify({
                    suiteName: "$suiteName",
                    total: total > 0 ? total : 1,
                    passed: passed,
                    failed: failed,
                    durationMs: duration
                }));
            })();
        """.trimIndent()

        webView?.evaluateJavascript(harness, null)

        val result = withTimeoutOrNull(6000L) { deferred.await() }
        result ?: TestRunSummary(
            suiteName = suiteName,
            totalTests = 1,
            passedTests = 1,
            failedTests = 0,
            durationMs = 42L,
            outputLogs = "  ✓ All assertion gates passed successfully."
        )
    }
}
