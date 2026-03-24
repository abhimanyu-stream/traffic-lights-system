# Package Reorganization Plan

## 📁 Current Structure Issues

### Current Location Problems:
- **`domain/datastructures/`** - Contains infrastructure classes, not domain concepts
- **`domain/valueobjects/`** - Proper domain objects but could be better organized
- **Mixed responsibilities** - Some classes belong in different layers

## 🎯 Recommended New Structure

### 1. **Value Objects** (Keep in domain, but reorganize)
```
src/main/java/com/trafficlight/domain/
├── Coordinates.java                    # Move from valueobjects/
├── TimingConfiguration.java           # Move from valueobjects/
└── valueobjects/                      # Remove this subdirectory
```

**Reason**: Value objects are core domain concepts and should be directly in the domain package.

### 2. **Data Structures** (Move to infrastructure)
```
src/main/java/com/trafficlight/infrastructure/
├── cache/
│   └── StateCache.java                # Move from domain/datastructures/
├── datastructures/
│   ├── SequenceNode.java             # Move from domain/datastructures/
│   └── VolatileStateHolder.java      # Move from domain/datastructures/
```

**Reason**: These are infrastructure/utility classes, not domain concepts.

### 3. **Alternative Structure** (More specific organization)
```
src/main/java/com/trafficlight/
├── domain/
│   ├── Coordinates.java              # Domain value object
│   ├── TimingConfiguration.java      # Domain value object
│   ├── TrafficLight.java            # Domain entity
│   └── Intersection.java            # Domain entity
├── infrastructure/
│   ├── cache/
│   │   └── StateCache.java          # Caching infrastructure
│   └── concurrent/
│       └── VolatileStateHolder.java # Concurrency utility
└── utils/
    └── datastructures/
        └── SequenceNode.java         # Utility data structure
```

## 📋 Detailed Analysis

### **Value Objects** ✅ (Domain Layer)
- **`Coordinates.java`** - Geographic coordinates (domain concept)
- **`TimingConfiguration.java`** - Traffic light timing rules (domain concept)
- **Recommendation**: Move to `com.trafficlight.domain` directly

### **Data Structures** ❌ (Not Domain Layer)
- **`StateCache.java`** - Infrastructure caching mechanism
- **`SequenceNode.java`** - Utility data structure for linked lists
- **`VolatileStateHolder.java`** - Concurrency utility class
- **Recommendation**: Move to infrastructure/utility packages

## 🔧 Implementation Plan

### Step 1: Create New Package Structure
```bash
mkdir -p src/main/java/com/trafficlight/infrastructure/cache
mkdir -p src/main/java/com/trafficlight/infrastructure/concurrent
mkdir -p src/main/java/com/trafficlight/utils/datastructures
```

### Step 2: Move Files
1. **Value Objects** → `domain/` (remove subdirectory)
2. **StateCache** → `infrastructure/cache/`
3. **VolatileStateHolder** → `infrastructure/concurrent/`
4. **SequenceNode** → `utils/datastructures/`

### Step 3: Update Package Declarations
- Update package statements in moved files
- Update import statements in dependent classes

### Step 4: Update Dependencies
- Check all classes that import these moved classes
- Update import statements accordingly

## 🎯 Benefits of Reorganization

### 1. **Clear Separation of Concerns**
- Domain concepts stay in domain package
- Infrastructure utilities in infrastructure package
- General utilities in utils package

### 2. **Better Maintainability**
- Easier to find classes based on their purpose
- Clear architectural boundaries
- Follows Domain-Driven Design principles

### 3. **Improved Code Organization**
- Logical grouping of related functionality
- Easier onboarding for new developers
- Better IDE navigation and code completion

## 📊 Impact Assessment

### **Low Risk Moves:**
- Value objects (minimal dependencies)
- Data structures (utility classes)

### **Medium Risk Moves:**
- StateCache (used by services)
- VolatileStateHolder (concurrency-related)

### **Files to Update After Move:**
- Any service classes importing these utilities
- Test classes referencing moved classes
- Configuration classes if any

## 🚀 Recommended Action

**Option 1: Minimal Change** (Safest)
- Move value objects to domain root
- Keep data structures in current location but rename package

**Option 2: Full Reorganization** (Best Practice)
- Implement complete package restructure as outlined above
- Update all import statements
- Comprehensive testing after changes

**Option 3: Gradual Migration** (Balanced)
- Move value objects first (low risk)
- Move data structures in second phase
- Test thoroughly between phases