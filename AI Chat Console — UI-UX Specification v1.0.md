# AI Chat Console — UI/UX Specification v1.0

## 1. Product concept

**AI Chat Console** — developer-oriented AI workspace, unifying:

- regular AI chat;
- conversation memory;
- resilient multi-LLM routing;
- execution observability;
- provider fallback visualization;
- light/dark themes;
- responsive desktop/mobile interface.

Key UX-principe:

> Chat first. Architecture visible when needed.

The user should be able to use the application as a regular AI chat, but, if necessary, see the technical details of the request fulfillment.

Main modes:

### Context Chat
Context-saving dialogue.

Backend concept:

```text
Conversation
    ↓
Message
    ↓
Chat Memory
    ↓
LLM
```

### Resilient Chat
Stateless/fault-tolerant запрос с автоматическим fallback:

```text
OpenAI ×3
    ↓
Anthropic
    ↓
Gemini
```

---

# 2. Target platforms

Проектируем интерфейс сразу для четырех диапазонов.

| Breakpoint | Width | UI mode |
|---|---:|---|
| Large Desktop | ≥1440 px | Full layout |
| Desktop | 1024–1439 px | Compact layout |
| Tablet | 768–1023 px | Collapsible panels |
| Mobile | <768 px | Single-column |

Main Figma frame:

```text
1440 × 900
```

Additional required frame:

```text
390 × 844
```

It is also advisable to check:

```text
1280 × 800
768 × 1024
```

For the first version, a separate tablet design is not required: Auto Layout and responsive rules are enough.
---

# 3. Desktop layout

Main grid:

```text
┌───────────────────────────────────────────────────────────────────────┐
│ Top Bar                                                        56 px │
├──────────────┬──────────────────────────────────┬─────────────────────┤
│              │                                  │                     │
│ Sidebar      │ Chat Workspace                   │ Run Details         │
│              │                                  │                     │
│ 240 px       │ flexible                         │ 296 px              │
│              │                                  │                     │
└──────────────┴──────────────────────────────────┴─────────────────────┘
```

### Desktop ≥1440

```text
Sidebar        240 px
Run Details    296 px
Gap            0
Chat           remaining width
```

With a width of 1440:

```text
1440
- 240 Sidebar
- 296 Run Details
= 904 px Chat workspace
```

This is a comfortable width for chat.

Maximum content width for messages within Chat:

```text
760 px
```

Messages should not stretch to cover the full 900+ pixels.

---

# 4. Main application shell

Structure:

```text
AppShell
├── TopBar
├── Sidebar
├── MainWorkspace
│   ├── ChatHeader
│   ├── MessageList
│   └── PromptComposer
└── RunDetailsPanel
```

For React later is transfer into:

```tsx
<AppShell>
    <Sidebar />
    <ChatWorkspace />
    <RunDetails />
</AppShell>
```

---

# 5. Top Bar

Height:

```text
56 px
```

Padding:

```text
Left/right: 16 px
```

Composition:

```text
◇ AI CHAT

                              Backend status
                              Theme
                              User
```

Desktop:

```text
┌─────────────────────────────────────────────────────────────────┐
│ ◇ AI CHAT                    ● API Healthy    ☀/☾     SA ▾      │
└─────────────────────────────────────────────────────────────────┘
```

## Backend status

States:

```text
● Healthy
● Degraded
● Offline
```

Tooltip / popover:

```text
Backend

Status       Healthy
Environment  Local
Version      1.0.0
API          localhost:8080
```

В будущем эти данные можно получать через Spring Boot Actuator.

---

# 6. Sidebar

Desktop width:

```text
240 px
```

Compact desktop:

```text
72 px collapsed
```

Padding:

```text
12 px
```

Structure:

```text
Logo

+ New chat

WORKSPACE

Context Chat
Resilient Chat

RECENT

Spring AI memory
REST API design
PostgreSQL indexes

────────────

Architecture
API Documentation
Settings

User profile
```

## Sidebar item

Height:

```text
36 px
```

Radius:

```text
8 px
```

Horizontal padding:

```text
10 px
```

States:

```text
Default
Hover
Active
Focus
Disabled
```

Active example:

```text
▌ Context Chat
```

или soft accent background без сильной заливки.

---

# 7. Main Chat Workspace

Структура:

```text
ChatWorkspace
├── ChatHeader
├── MessagesViewport
└── ComposerArea
```

---

# 8. Chat Header

Height:

```text
64 px
```

Desktop Context mode:

```text
Context Chat
● Memory enabled      conv_8f4c...
```

Desktop Resilient mode:

```text
Resilient Chat
OpenAI → Anthropic → Gemini
```

Справа:

```text
[ Run details ]   ⋯
```

Если правая панель уже открыта:

```text
[ Hide details ]
```

---

# 9. Chat mode selector

На Empty Screen и при New Chat показываем mode selector.

```text
┌────────────────────────────────────────────────┐
│ ● Context Chat       ◇ Resilient Chat          │
│   Memory enabled       Automatic fallback      │
└────────────────────────────────────────────────┘
```

Desktop width:

```text
420–520 px
```

На mobile:

```text
Context
Resilient
```

как segmented control.

Каждый mode имеет tooltip/info panel.

### Context

```text
Conversation memory

The AI remembers previous messages
inside this conversation.
```

### Resilient

```text
Reliable multi-provider execution

OpenAI
up to 3 attempts

↓ fallback

Anthropic

↓ fallback

Gemini
```

---

# 10. Empty Chat State

Главная композиция:

```text
                     ◇

              What can I help with?

     Ask questions, explore code or architecture

     ┌──────────────────────────────────────┐
     │ Ask anything...                      │
     │                                      │
     │ Context Chat                     ↑   │
     └──────────────────────────────────────┘

     Explain Spring AI    Review Java code
     Design REST API      Optimize PostgreSQL
```

Vertical center должен находиться немного выше геометрического центра экрана:

```text
~42% screen height
```

Это визуально ощущается лучше.

---

# 11. Message List

Максимальная ширина:

```text
760 px
```

Центрирование:

```text
margin-inline: auto
```

Horizontal padding desktop:

```text
32 px
```

Tablet:

```text
24 px
```

Mobile:

```text
16 px
```

Spacing between messages:

```text
28–32 px
```

---

# 12. User Message

Не используем классический Telegram bubble.

Structure:

```text
SA   YOU                          16:01

Can you explain how Spring AI
conversation memory works?
```

Container:

```text
background: Surface Subtle
border-radius: 12 px
padding: 14 px 16 px
```

Max width:

```text
85%
```

---

# 13. Assistant Message

Более открытый layout:

```text
◇ AI                              16:01

Spring AI conversation memory allows
an application to provide previous
messages to the model...

[Copy] [Retry]
```

Без тяжелой bubble-заливки.

Content area:

```text
max-width: 100%
```

Actions появляются:

- всегда на touch devices;
- по hover на desktop;
- после завершения генерации.

---

# 14. Markdown rendering

Поддерживаются:

```text
H1–H4
paragraph
bold
italic
lists
ordered lists
blockquote
links
inline code
code blocks
tables
horizontal rule
```

Для portfolio особенно важны:

```text
Java
TypeScript
SQL
JSON
YAML
Bash
Dockerfile
```

---

# 15. Code Block

Structure:

```text
┌──────────────────────────────────────────────┐
│ Java                                   Copy │
├──────────────────────────────────────────────┤
│ @Service                                     │
│ public class ChatService {                   │
│                                              │
│ }                                            │
└──────────────────────────────────────────────┘
```

Header:

```text
32 px
```

Body padding:

```text
16 px
```

Radius:

```text
10 px
```

Monospace:

```text
JetBrains Mono
```

Fallback:

```text
ui-monospace
```

---

# 16. Prompt Composer

Desktop:

```text
max-width: 760 px
```

Minimum height:

```text
56 px
```

Maximum expanded height:

```text
200 px
```

Structure:

```text
┌──────────────────────────────────────────────┐
│ Ask anything...                              │
│                                              │
│ Context Chat                         ↑       │
└──────────────────────────────────────────────┘
```

Radius:

```text
14 px
```

Padding:

```text
12 px
```

Send button:

```text
36 × 36 px
```

States:

```text
Disabled
Ready
Sending
Stop generation
```

Keyboard:

```text
Enter         Send
Shift+Enter   New line
```

---

# 17. Run Details panel

Desktop width:

```text
296 px
```

Min:

```text
272 px
```

Max:

```text
320 px
```

Scrollable independently.

Structure:

```text
RUN DETAILS

Status
● Completed

Provider
OpenAI

Model
gpt-...

Mode
Resilient

Duration
842 ms

Fallback
No

Attempts
1

REQUEST

req_01J...

PROVIDER TRACE

OpenAI
● Success   842 ms

Anthropic
○ Not used

Gemini
○ Not used
```

---

# 18. Provider Trace

Success:

```text
OpenAI
│
● Success
  842 ms

Anthropic
○ Not used

Gemini
○ Not used
```

Fallback:

```text
OpenAI
│
├─ × Attempt 1
│    812 ms
│
├─ × Attempt 2
│    906 ms
│
└─ × Attempt 3
     794 ms
     │
     ▼
Anthropic
│
● Success
  1.4 s

Gemini
○ Not used
```

States:

```text
Pending
Running
Success
Failed
Skipped
```

Никаких ярких brand colors поставщиков.

Цвет кодирует именно status.

---

# 19. Execution API model

Backend рекомендуется привести к единому response contract.

```json
{
  "message": {
    "id": "msg_01J...",
    "role": "assistant",
    "content": "Response text",
    "createdAt": "2026-08-07T16:02:31+07:00"
  },
  "execution": {
    "requestId": "req_01J...",
    "mode": "RESILIENT",
    "provider": "ANTHROPIC",
    "model": "claude-sonnet",
    "status": "SUCCESS",
    "fallbackUsed": true,
    "attemptCount": 4,
    "durationMs": 4231,
    "attempts": [
      {
        "provider": "OPENAI",
        "attempt": 1,
        "status": "FAILED",
        "durationMs": 812
      },
      {
        "provider": "OPENAI",
        "attempt": 2,
        "status": "FAILED",
        "durationMs": 906
      },
      {
        "provider": "OPENAI",
        "attempt": 3,
        "status": "FAILED",
        "durationMs": 794
      },
      {
        "provider": "ANTHROPIC",
        "attempt": 1,
        "status": "SUCCESS",
        "durationMs": 1719
      }
    ]
  }
}
```

---

# 20. Conversation API

Recommended:

```text
POST /api/conversations
```

Response:

```json
{
  "id": "conv_01J...",
  "title": null,
  "createdAt": "..."
}
```

Messages:

```text
POST /api/conversations/{conversationId}/messages
```

History:

```text
GET /api/conversations/{conversationId}/messages
```

Conversation list:

```text
GET /api/conversations
```

Delete:

```text
DELETE /api/conversations/{conversationId}
```

Это позволит sidebar Recent Chats опираться на backend, а не LocalStorage.

---

# 21. Light / Dark theme

Поддерживаются:

```text
LIGHT
DARK
SYSTEM
```

Theme selection хранится frontend-side.

---

# 22. Dark Theme Tokens

```text
bg.canvas        #09090B
bg.surface       #111318
bg.elevated      #17191F
bg.subtle        #1B1D23

border.default   #292C33
border.strong    #363942

text.primary     #F5F5F6
text.secondary   #A1A1AA
text.muted       #71717A

accent.primary   #7C5CFC
accent.hover     #8B70FF
accent.soft      #292342
```

Semantic colors:

```text
success
warning
error
info
```

Цвета задаются semantic variables, а не непосредственно компонентам.

---

# 23. Light Theme Tokens

```text
bg.canvas        #F7F8FA
bg.surface       #FFFFFF
bg.elevated      #FFFFFF
bg.subtle        #F1F2F4

border.default   #E2E4E9
border.strong    #D2D5DC

text.primary     #18181B
text.secondary   #71717A
text.muted       #A1A1AA

accent.primary   #6952E8
accent.hover     #5D47D8
accent.soft      #EFEDFF
```

---

# 24. Theme switch

TopBar quick control:

```text
☀ / ☾
```

Settings:

```text
Appearance

Theme

○ Light
○ Dark
● System
```

System использует:

```text
prefers-color-scheme
```

---

# 25. Typography

Primary UI font:

```text
Inter
```

Alternative:

```text
Geist
```

Recommended:

```text
Geist
```

Typography scale:

```text
Display      32 / 40 / 600
Title        20 / 28 / 600
Heading      16 / 24 / 600
Body         14 / 22 / 400
Body Strong  14 / 22 / 500
Small        12 / 18 / 400
Label        12 / 16 / 500
```

Code:

```text
JetBrains Mono
13 / 20
```

---

# 26. Spacing system

Используем 4px baseline:

```text
4
8
12
16
20
24
32
40
48
64
```

Основные:

```text
Component internal       8–12
Card padding             16
Panel padding            16
Page horizontal          24–32
Major sections           32
```

---

# 27. Border radius

```text
xs     4
sm     6
md     8
lg     12
xl     14
2xl    18
round  999
```

Recommended:

```text
Buttons           8
Inputs            10
Cards             12
Prompt Composer   14
Modal             14
```

---

# 28. Shadows

Dark mode — практически без shadows.

Используем:

```text
border + contrast
```

Light mode:

```text
small elevated surfaces:
0 1px 2px rgba(...)
```

Без тяжелого material-style shadow.

---

# 29. Figma Variables

Создать collections:

```text
Colors
Spacing
Radius
Typography
Layout
```

Color modes:

```text
Light
Dark
```

Не называть переменную:

```text
purple-500
```

В компонентах использовать semantic naming:

```text
accent.primary
text.primary
bg.surface
border.default
status.success
status.error
```

---

# 30. Component Library

Обязательные Figma components:

```text
Button
IconButton
Input
Textarea
Avatar
Badge
Tooltip
Popover
Dropdown
SegmentedControl
Tabs
Toast
Modal
Skeleton
Spinner

Sidebar
SidebarItem
SidebarSection

ChatHeader
ChatMessage/User
ChatMessage/Assistant
PromptComposer
PromptSuggestion

StatusIndicator
ModeSelector

RunDetails
ExecutionProperty
ProviderTrace
ProviderStep

CodeBlock
MarkdownTable

ThemeSelector
UserMenu
```

---

# 31. Button variants

```text
Primary
Secondary
Ghost
Danger
```

Sizes:

```text
SM  32 px
MD  36 px
LG  40 px
```

States:

```text
Default
Hover
Pressed
Focus
Disabled
Loading
```

---

# 32. Responsive behaviour

Responsive design является частью v1, даже если mobile coding реализуется позднее.

## ≥1440 px

```text
Sidebar        visible 240
Chat           flexible
Run Details    visible 296
```

---

## 1024–1439 px

Sidebar:

```text
collapsed = 72 px
```

Run Details:

```text
280 px
```

или закрыта по умолчанию при 1024–1199.

Layout:

```text
72 | Chat | 280
```

---

## 768–1023 px

Sidebar становится drawer.

Default:

```text
Chat = 100%
Sidebar = hidden
Run Details = hidden
```

Top bar:

```text
☰      Context Chat               Details
```

Sidebar открывается поверх контента:

```text
280 px drawer
```

Run Details тоже:

```text
320 px right drawer
```

---

# 33. Mobile layout <768 px

Полностью single-column.

```text
┌─────────────────────────────┐
│ ☰  Context Chat       ☾  ⋯ │
├─────────────────────────────┤
│                             │
│ Message                     │
│                             │
│ AI response                 │
│                             │
│                             │
│                             │
├─────────────────────────────┤
│ Ask anything...          ↑ │
└─────────────────────────────┘
```

Sidebar не занимает место постоянно.

Открывается:

```text
Left Drawer
```

Run Details открывается:

```text
Bottom Sheet
```

Это важный момент.

Не делать справа 300px panel на телефоне.

---

# 34. Mobile Run Details

Bottom sheet:

```text
╭─────────────────────────────╮
│ ─────                       │
│ Run Details                 │
│                             │
│ ● Completed                 │
│ Provider   Anthropic        │
│ Duration   4.2 s            │
│ Fallback   Yes              │
│                             │
│ Provider Trace              │
│ OpenAI                      │
│ × × ×                       │
│ ↓                           │
│ Anthropic                   │
│ ● Success                   │
│                             │
╰─────────────────────────────╯
```

Sheet heights:

```text
Collapsed    40%
Expanded     85%
```

---

# 35. Mobile Prompt Composer

Fixed/sticky bottom.

```text
┌────────────────────────────┐
│ Ask anything...            │
│                            │
│ Context                ↑   │
└────────────────────────────┘
```

Horizontal margin:

```text
12 px
```

Bottom spacing учитывает:

```text
safe-area-inset-bottom
```

---

# 36. Mobile Mode Selector

Desktop card превращается в segmented control:

```text
┌──────────────────────────┐
│ Context    │  Resilient  │
└──────────────────────────┘
```

Пояснение показывается через:

```text
ⓘ
```

или modal/bottom sheet.

---

# 37. Mobile message typography

Body:

```text
15 / 23
```

UI labels:

```text
12–13
```

Code:

```text
12.5 / 19
```

Code blocks:

```text
horizontal scroll
```

Нельзя уменьшать code до нечитаемого размера.

---

# 38. Architecture Page

Desktop:

```text
Architecture

                    React + TypeScript
                           │
                           ▼
                      REST API
                           │
                           ▼
                     Spring Boot
                           │
              ┌────────────┴────────────┐
              │                         │
              ▼                         ▼
        Context Engine            Resilient Engine
              │                         │
              ▼                         ▼
         Chat Memory                 OpenAI
              │                      × 3 retry
              ▼                         │
        JDBC Repository                 ▼
                                    Anthropic
                                        │
                                        ▼
                                      Gemini
```

Mobile:

диаграмма перестраивается вертикально:

```text
React
 ↓
Spring Boot
 ↓
 ├ Context
 │   ↓
 │ Memory
 │
 └ Resilient
     ↓
    OpenAI
     ↓
    Anthropic
     ↓
    Gemini
```

Никакого horizontal scrolling всей страницы.

---

# 39. Settings page

Sections:

```text
Appearance
API
Chat
About
```

### Appearance

```text
Theme
Light / Dark / System
```

### Chat

```text
Default mode

Context
Resilient
```

### API

```text
Backend URL
API Status
Swagger
```

### About

```text
AI Chat Console
Version

Frontend:
React
TypeScript

Backend:
Java
Spring Boot
Spring AI
```

---

# 40. Login Screen

Desktop:

```text
┌──────────────────────────────────────────────────┐
│                                                  │
│                     ◇                            │
│                                                  │
│                 AI Chat Console                  │
│                                                  │
│       Multi-model AI developer workspace         │
│                                                  │
│       Username                                   │
│       ┌─────────────────────────────┐            │
│       │                             │            │
│       └─────────────────────────────┘            │
│                                                  │
│       Password                                   │
│       ┌─────────────────────────────┐            │
│       │                             │            │
│       └─────────────────────────────┘            │
│                                                  │
│       [         Sign in             ]            │
│                                                  │
│               ☀ Light / Dark                     │
│                                                  │
└──────────────────────────────────────────────────┘
```

Mobile uses exactly the same card logic without a desktop-only redesign.

---

# 41. Loading states

Chat request:

```text
◇ AI

● Thinking...
```

Resilient mode can show richer progress:

```text
OpenAI
◌ Attempt 2 of 3
```

Do not expose excessive internal debugging data in the actual message.

Execution status belongs primarily in Run Details.

---

# 42. Error states

Example:

```text
Unable to complete request

All configured AI providers failed.

[ Retry ]
[ View details ]
```

Run Details:

```text
OpenAI      Failed
Anthropic   Failed
Gemini      Failed
```

Backend error details should be sanitized.

Не выводим exception stacktrace frontend-пользователю.

---

# 43. Toasts

Use for:

```text
Message copied
Conversation deleted
Settings saved
Connection restored
```

Не использовать toast для серьезного blocking error.

---

# 44. Accessibility

Минимальные требования:

```text
WCAG AA contrast
Keyboard navigation
Visible focus states
aria-label для icon-only controls
Touch target минимум 44 × 44 px
```

Особенно важно:

```text
Theme
Sidebar
Run Details
Send
Copy
Retry
```

---

# 45. Animation

Очень умеренная.

Duration:

```text
120–200 ms UI
200–300 ms drawer
```

Можно:

```text
Sidebar collapse
Run Details slide
Theme transition
Message appearance
Provider trace progression
```

Нельзя:

```text
continuous glowing
floating cards
parallax
heavy glass animation
```

---

# 46. Figma Pages

Организация файла:

```text
00 — Cover

01 — Foundations
     Colors
     Typography
     Spacing
     Grid
     Icons

02 — Components
     Buttons
     Inputs
     Navigation
     Chat
     Observability

03 — Desktop
     Login
     Empty
     Context
     Resilient
     Resilient Fallback
     Architecture
     Settings

04 — Mobile
     Login
     Empty
     Chat
     Sidebar
     Run Details
     Settings

05 — Prototype

06 — Dev Handoff
```

---

# 47. Required Desktop Frames

```text
D01 Login
D02 Empty Chat
D03 Context Conversation
D04 Resilient Success
D05 Resilient Fallback
D06 Architecture
D07 Settings
```

Size:

```text
1440 × 900
```

---

# 48. Required Mobile Frames

Для первого design milestone достаточно четырех:

```text
M01 Empty Chat
M02 Conversation
M03 Navigation Drawer
M04 Run Details Sheet
```

Size:

```text
390 × 844
```

Login и Settings можно адаптировать после утверждения основных layouts.

---

# 49. Desktop grid

Frame:

```text
1440
```

Grid:

```text
12 columns
Margin 32
Gutter 24
```

Но App Shell sidebar/right panel не обязаны жестко следовать 12-column grid.

Grid прежде всего используется внутри content pages.

---

# 50. Responsive implementation rule

Компоненты не должны иметь desktop/mobile версии без необходимости.

Предпочтительно:

```text
One component
+
responsive variants
```

Например:

```tsx
<Sidebar />
```

ведет себя как:

```text
Desktop → fixed panel
Tablet  → drawer
Mobile  → drawer
```

А:

```tsx
<RunDetails />
```

ведет себя:

```text
Desktop → side panel
Tablet  → overlay drawer
Mobile  → bottom sheet
```

Это ключевой архитектурный принцип frontend.

---

# 51. Recommended frontend mapping

Design component:

```text
PromptComposer
```

React:

```text
features/chat/components/PromptComposer.tsx
```

Design:

```text
ProviderTrace
```

React:

```text
features/execution/components/ProviderTrace.tsx
```

Design:

```text
ModeSelector
```

React:

```text
features/chat-mode/components/ChatModeSelector.tsx
```

Design:

```text
RunDetails
```

React:

```text
features/execution/components/RunDetails.tsx
```

---

# 52. Suggested future frontend stack

```text
React
TypeScript
Vite

React Router
TanStack Query

Zustand

React Hook Form
Zod

Tailwind CSS
shadcn/ui

Lucide
react-markdown
syntax highlighter
```

Не требуется:

```text
Redux
Material UI
large dashboard framework
```

---

# 53. MVP priorities

## Phase 1 — Design System

```text
Colors
Themes
Typography
Spacing
Buttons
Inputs
Navigation
Chat messages
Composer
```

## Phase 2 — Desktop Figma

```text
Empty
Context
Resilient
Fallback
```

## Phase 3 — Observability

```text
Run Details
Provider Trace
Status
```

## Phase 4 — Responsive

```text
Collapsed Sidebar
Tablet Drawer
Mobile Chat
Bottom Sheet
```

## Phase 5 — Secondary screens

```text
Login
Architecture
Settings
```

---

# 54. Important scope rule

Функциональность проекта должна показывать:

```text
AI Chat
+
Multi-LLM resilience
+
Conversation memory
+
Frontend architecture
+
Responsive UI
```

Она не должна превращаться в полноценную AI SaaS platform.

Не добавляем в MVP:

```text
billing
teams
subscriptions
agents
knowledge bases
image generation
file management
prompt marketplace
complex analytics
```

---

# 55. Final visual direction

Style:

```text
Modern
Technical
Minimal
Premium
Developer-oriented
```

Reference feeling:

```text
Linear
Vercel
modern IDE tooling
AI developer consoles
```

Но не визуальное копирование какого-либо продукта.

Основные характеристики:

```text
Dark-first, but complete Light theme

High information density without clutter

Neutral surfaces

Single restrained accent

Minimal gradients

Clear typography

Strong component hierarchy

Subtle borders

Very limited shadows

Technical execution data available,
but never dominant over conversation
```

---

# 56. Product identity

Working product name:

**AI Chat Console**

Descriptor:

**Multi-LLM AI Workspace**

Portfolio subtitle:

> Conversational memory when context matters.  
> Automatic provider fallback when reliability matters.

Short architecture statement:

> A responsive full-stack AI workspace built with React, TypeScript, Spring Boot and Spring AI, featuring conversational memory, multi-provider failover and execution observability.

---

# 57. Definition of Done for Figma v1

Figma v1 is complete, when:

- Light + Dark variables;
- Desktop 1440 design;
- responsive component constraints;
- Context Chat;
- Resilient Chat;
- Resilient fallback scenario;
- Run Details;
- Provider Trace;
- Empty state;
- prompt composer;
- code block;
- sidebar;
- mobile conversation screen;
- mobile navigation drawer;
- mobile Run Details bottom sheet;
- Architecture screen;
- reusable component library.

After when design can be safety transferred into React without architecture changes.