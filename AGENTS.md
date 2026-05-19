# AGENTS.md

# HappyRide Android Project — AI Agent Rules & Engineering Guide

## Purpose of this Document

This document defines the mandatory engineering rules, architectural constraints, implementation philosophy, collaboration expectations, performance strategies, connectivity strategies, and documentation requirements that any AI assistant, coding agent, or automated development tool MUST follow while working on this project.

This project is NOT an AI playground.

The AI is NOT the owner of the architecture.
The AI is NOT allowed to redesign the application.
The AI is NOT allowed to make uncontrolled refactors.
The AI is NOT allowed to impose its own architecture.

The AI acts as a surgical engineering assistant.

The primary mission of the AI is:

* preserve stability
* preserve teammates code
* preserve architecture
* preserve maintainability
* preserve UX
* preserve offline behavior
* preserve performance
* avoid connectivity anti-patterns
* avoid performance bugs
* explain engineering decisions clearly
* help future documentation efforts

This document is intentionally strict.

---

# Project Overview

HappyRide is a mobile carpooling application developed for Android using Kotlin and Jetpack Compose.

The app focuses on:

* ride sharing
* driver/rider coordination
* real-time or near-real-time rider visibility
* location sharing
* trip management
* eventual connectivity
* mobile performance
* responsive UX
* fault tolerance
* analytics

The project is developed in an academic software architecture environment.

Therefore:

* architectural consistency matters
* maintainability matters
* explainability matters
* documentation matters
* engineering justification matters
* performance profiling matters
* offline strategies matter

This project uses:

* Kotlin
* Jetpack Compose
* MVVM architecture
* Retrofit
* Supabase
* PostgreSQL
* Mapbox
* Coroutines
* Material 3

---

# ABSOLUTE GOLDEN RULES

## Rule 1 — NEVER modify teammates code unnecessarily

The AI MUST preserve the existing implementation whenever possible.

The AI MUST:

* prefer extension over replacement
* prefer minimal changes
* avoid rewriting working code
* avoid architectural redesigns
* avoid code cleanup unless requested
* avoid stylistic rewrites
* avoid formatting unrelated code
* avoid “modernization” refactors

If something already works:
DO NOT rewrite it.

---

## Rule 2 — NEVER remove or modify existing comments

This is a STRICT rule.

The AI MUST NEVER:

* delete comments
* rewrite comments
* reformat comments
* “improve” comments
* translate comments
* move comments unnecessarily

Comments written by teammates are protected.

The AI MAY add NEW comments only if:

* the user explicitly asks
* the logic is highly complex
* documentation is required

When adding new comments:

* NEVER use emojis
* comments must be concise
* comments must be technical
* comments must explain intent, not obvious syntax

---

## Rule 3 — Refactors are PROHIBITED unless explicitly requested

The AI MUST NOT:

* rename classes
* rename variables
* rename DTOs
* rename methods
* move packages
* split files
* merge files
* reorganize architecture
* replace patterns
* migrate frameworks
* introduce new architectural layers

UNLESS the user explicitly requests a refactor.

Files CREATED by the AI can internally follow the AI's preferred structure.
However:

* they MUST integrate with the existing architecture
* they MUST respect package organization
* they MUST respect naming conventions already used in the project

---

## Rule 4 — Always explain changes

Whenever the AI modifies code, it MUST clearly explain:

* what files were changed
* why they were changed
* what functionality was added
* what constraints were preserved
* what anti-patterns were avoided
* what performance considerations were taken
* what connectivity considerations were taken
* what tradeoffs were chosen

This project requires future academic documentation.

Therefore:
all engineering decisions MUST be explainable.

---

# Architecture Rules

## Architecture Style

This project uses MVVM.

The AI MUST preserve MVVM separation.

---

## Expected Package Organization

presentation/

* views/
* components/
* viewmodels/
* navigation/

core/

* shared utilities
* shared abstractions

data/

* dto/
* repositories/
* services/
* local/

domain/

* business/domain models only

---

## MVVM Enforcement Rules

The AI MUST:

* keep UI logic lightweight
* keep business logic outside composables
* keep networking outside UI
* keep state inside ViewModels
* use repositories as data access layer
* keep Retrofit access outside UI
* preserve unidirectional data flow whenever possible

The AI MUST NOT:

* perform heavy computation inside composables
* perform networking directly in composables
* directly call Retrofit from UI
* place backend logic in Compose screens
* place Mapbox business logic directly in UI if avoidable

---

# Compose Rules

Compose screens must remain clean and maintainable.

The AI SHOULD:

* prefer reusable composables
* keep composables focused
* avoid giant composables
* minimize recompositions
* preserve existing UI style
* preserve existing flows

The AI MUST NOT:

* inject excessive business logic into composables
* perform expensive loops in composables
* block UI rendering
* create uncontrolled state mutations

---

# Connectivity & Offline Philosophy

Connectivity handling is one of the MOST IMPORTANT aspects of this project.

The application MUST support eventual connectivity strategies.

The project philosophy is:

"The application must degrade gracefully under unstable connectivity conditions."

The user must NEVER feel trapped.

The UI must NEVER collapse silently.

The app must ALWAYS attempt to:

* preserve functionality
* preserve feedback
* preserve responsiveness
* preserve user trust

---

# Connectivity Anti-Patterns — STRICTLY FORBIDDEN

The AI MUST NEVER introduce implementations that produce the following anti-patterns:

## Blocked Application

The app MUST NEVER freeze while waiting for internet.

---

## Stuck Progress Notification

Infinite loading indicators are forbidden.

The UI MUST always:

* timeout
* retry
* fail gracefully
* notify users

---

## Non informative message

Generic errors are forbidden.

The AI MUST:

* provide meaningful user feedback
* avoid technical jargon in UI
* avoid raw backend errors
* distinguish timeout vs auth vs offline vs server issues

---

## Lost Content

User actions or map states MUST NOT disappear silently.

Whenever possible:

* cache state
* preserve temporary data
* preserve latest known information

---

## Non-existent Result Notification

The app MUST notify:

* success
* failure
* retry
* loading
* fallback state
* offline state

---

## Redirection without connectivity check

The AI MUST NEVER redirect users to:

* web pages
* external resources
* map resources
* online-only features

without validating connectivity first.

---

## Unavailable functionality after connection recovery

Features MUST recover correctly after connectivity returns.

The AI MUST think about:

* retry states
* stale cache invalidation
* state restoration
* reconnection flow

---

## Unclear Behaviour

The app MUST behave predictably under unstable connectivity.

The AI MUST avoid:

* duplicated actions
* random refreshes
* inconsistent states
* race conditions
* delayed unordered UI updates

---

## Unexpected result from Background Process

Background queues MUST NOT execute repeatedly or unexpectedly after reconnection.

The AI MUST avoid:

* infinite retries
* uncontrolled worker loops
* duplicated synchronization jobs
* duplicated uploads

---

## Browser Embedded Incorrectly

Web content MUST fail gracefully.

---

## Map file embedded incorrectly

Maps MUST NEVER silently fail.

If map resources fail:

* explain the issue
* preserve the screen
* preserve UX
* provide fallback UI
* use cached information if available

---

# Offline Development Workflow

Offline strategies are HIGH IMPACT architectural decisions.

The AI MUST NOT autonomously decide the complete offline strategy.

Instead:

* the AI MUST propose alternatives
* the AI MUST explain tradeoffs
* the AI MUST ask the user before implementing major offline persistence behavior

Examples:

* cache-first
* network-first
* disk persistence
* synchronization queue
* retry policies
* stale cache policies
* recovery flow

The user MUST participate in these decisions.

---

# Connectivity Implementation Expectations

The project is heavily inspired by eventual connectivity principles.

The AI SHOULD use:

* coroutines
* callbacks
* asynchronous workers
* graceful fallback UI
* cached last known state
* retry actions
* local persistence when needed

The AI MUST prioritize:

* responsiveness
* user trust
* fault tolerance
* predictable UX

---

# Performance Engineering Rules

Performance is a FIRST CLASS requirement in this project.

The AI MUST actively avoid introducing:

* GUI lag
* ANRs
* memory bloats
* energy bugs
* overdraw
* unnecessary allocations
* heavy main-thread work
* uncontrolled polling

The project follows the engineering principles discussed in Chapters 11 and 12 regarding mobile performance engineering. fileciteturn2file0L1-L10 fileciteturn2file1L1-L15

---

# Main Thread Rules

The UI thread is sacred.

The AI MUST NEVER:

* execute long operations on main thread
* perform heavy processing in Compose rendering
* block rendering while waiting for network
* execute expensive loops in UI
* decode large resources synchronously
* perform database/network operations in composables

Heavy tasks MUST be offloaded.

The AI MUST use:

* coroutines
* asynchronous execution
* background workers
* IO dispatchers

The AI MUST understand that blocking the main thread causes:

* GUI lag
* ANRs
* frozen UI
* poor UX

As discussed in the performance chapters. fileciteturn2file0L3-L4 fileciteturn2file1L8-L10

---

# Coroutines & Async Programming

The project strongly prefers asynchronous programming.

The AI SHOULD:

* use suspend functions
* use coroutines
* use appropriate dispatchers
* preserve structured concurrency

The AI MUST:

* avoid blocking calls
* avoid synchronous network execution
* avoid uncontrolled background threads
* avoid callback hell when coroutines already exist

The AI MUST explain:

* why asynchronous execution was necessary
* what work was moved off main thread
* what UI feedback was preserved

---

# Multi-threading Rules

Workers/background tasks MUST be carefully designed.

The AI MUST:

* justify background workers
* explain lifecycle implications
* avoid runaway loops
* avoid immortal background behavior
* avoid duplicated worker execution
* avoid unnecessary polling

The AI MUST understand:

* Android is single-thread oriented by default
* GUI operations belong on main thread
* long tasks belong off main thread

---

# Caching Rules

Caching is encouraged.

However:

* caching decisions MUST be justified
* caching policies MUST be documented
* stale data behavior MUST be explained

The AI SHOULD consider:

* memory cache
* disk cache
* last known state
* lightweight persistence
* cache invalidation strategies

The AI MUST explain:

* why cache exists
* what is cached
* when cache expires
* what happens offline
* how fallback behavior works

---

# Memory Management Rules

The AI MUST actively avoid memory bloats.

The AI MUST:

* avoid unnecessary object creation
* avoid large allocations in loops
* avoid memory leaks
* avoid strong reference cycles
* release listeners properly
* release location listeners properly
* avoid storing giant collections unnecessarily

The AI SHOULD:

* prefer memory efficient structures when appropriate
* avoid excessive wrapper objects
* avoid unnecessary bitmap retention

---

# Energy Consumption Rules

Battery usage matters.

The AI MUST avoid:

* aggressive GPS polling
* endless retries
* runaway synchronization loops
* listeners that never unregister
* unnecessary background work
* infinite refresh cycles

The AI MUST be especially careful with:

* location listeners
* Mapbox updates
* background workers
* polling frequency

The AI MUST understand that GPS and background work are energy-sensitive resources. fileciteturn2file0L7-L8

---

# Overdraw & UI Performance

The AI SHOULD:

* avoid unnecessary nested layouts
* avoid unnecessary transparency layers
* avoid redundant backgrounds
* keep UI rendering lightweight

The AI MUST understand that excessive UI layering can cause rendering lag.

---

# Bugfixing Philosophy

Bugfixing is NOT only about making the error disappear.

The AI MUST:

* identify root causes
* explain the root cause
* explain why the fix works
* explain possible regressions
* explain affected layers

The project follows the engineering mentality from Chapter 12:

* identify performance bugs
* prevent recurrence
* document engineering reasoning
* profile when necessary
* justify optimizations

---

# Profiling & Diagnostics Rules

When performance issues appear, the AI SHOULD think about:

* CPU usage
* memory usage
* recompositions
* overdraw
* background workers
* ANRs
* network latency
* map rendering
* location updates

The AI SHOULD explain:

* what should be profiled
* why it matters
* what Android Studio profiler/tool can be used

---

# Mapbox Rules

Map functionality is critical.

The AI MUST:

* preserve map responsiveness
* avoid blocking rendering
* avoid aggressive refresh loops
* preserve fallback UI
* preserve cached last-known locations when useful
* preserve graceful degradation

The AI MUST NOT:

* silently fail map rendering
* freeze UI during map loading
* spam location updates
* aggressively poll backend for locations

---

# Supabase Rules

The AI MUST preserve:

* existing endpoints
* existing authentication flow
* existing DTO contracts
* existing Retrofit services
* existing repository patterns

The AI MUST NOT:

* change endpoints without permission
* modify auth flow arbitrarily
* redesign backend contracts

---

# Strictly Forbidden Actions

The AI MUST NEVER:

* rename DTOs
* rename variables unnecessarily
* move packages
* change gradle versions
* touch local.properties
* delete “unused” code
* modify AndroidManifest.xml without explicit explanation
* modify teammate comments
* introduce hidden architectural changes

If AndroidManifest changes are needed:

* explain WHY
* explain WHAT
* explain RISKS
* explain PERMISSIONS affected

---

# Documentation-Aware Development

This project requires future documentation.

Therefore, the AI MUST generate engineering decisions that are:

* explainable
* traceable
* documentable
* academically defensible

The AI SHOULD explain:

* what anti-pattern was avoided
* what strategy was used
* what tradeoff was selected
* what performance concern existed
* what connectivity concern existed
* why a worker/coroutine/cache exists
* why fallback behavior exists

The AI MUST think:
"Could the team later explain this in a wiki, sprint report, or presentation?"

---

# Change Reporting Format

Whenever implementing something non-trivial, the AI SHOULD provide:

## Files Modified

* file list

## Why Each File Changed

* brief explanation

## Architectural Constraints Preserved

* MVVM
* Compose separation
* existing flows
* etc.

## Connectivity Considerations

* retries
* fallback
* cache
* graceful degradation

## Performance Considerations

* threading
* background work
* memory
* rendering
* battery

## Anti-Patterns Avoided

* blocked UI
* ANRs
* infinite loading
* etc.

---

# Team Collaboration Philosophy

This is a collaborative codebase.

The AI MUST behave conservatively.

The AI MUST assume:

* teammates depend on stability
* teammates depend on file structure
* teammates depend on naming consistency
* teammates depend on predictable architecture

The AI MUST prioritize:

* compatibility
* stability
* readability
* maintainability
* minimal disruption

---

# Domain Model Understanding

The AI MUST understand the business domain before implementing features.

Core concepts include:

## Driver

User offering a ride.

## Rider

User joining a ride.

## Ride

A trip created by a driver.

## Reservation

A rider request/association to a ride.

## Shared Location

Temporary rider/driver location visibility during trips.

## Trip Map

Mapbox-based visualization of participants.

## Eventual Connectivity

The app may temporarily lose connectivity and MUST degrade gracefully.

## Fallback UI

The UI must still communicate useful information during failures.

## Last Known Location

Cached rider/driver state used during degraded connectivity.

---

# Final Philosophy

The goal is NOT to create the fanciest architecture.

The goal is:

* preserve UX
* preserve responsiveness
* preserve stability
* preserve teamwork
* preserve explainability
* preserve maintainability
* avoid connectivity disasters
* avoid performance bugs
* build trustworthy mobile experiences

The AI MUST behave like a careful senior mobile engineer working inside a collaborative academic architecture project.

Not like an uncontrolled autonomous refactoring engine.
