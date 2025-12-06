# Development Preferences

## Test-Driven Development (TDD)

For all backend code changes:
1. Write tests FIRST before implementation
2. Run tests to verify they fail (red)
3. Implement minimal code to make tests pass (green)
4. Refactor if needed
5. Commit tests and implementation together

Exceptions where TDD can be skipped:
- Frontend UI/CSS changes
- Configuration files
- Documentation
- Prototypes explicitly marked as such
- When user explicitly says "skip tests for now"

## Required Test Types

### Unit Tests (Always Required)
- All service layer methods
- All utility/helper functions
- All business logic
- Validation logic

### Contract Tests (Required for APIs)
- All REST API endpoints (controllers)
- Request/response validation
- HTTP status codes
- Error handling
- API contracts must be tested before implementation

### Integration Tests (When Applicable)
- Database interactions
- External service calls
- Full request/response cycles

## Test Coverage Goals

- Aim for meaningful coverage, not arbitrary percentages
- Every public method should have at least one test
- Every bug fix requires a regression test
- Tests should be clear, maintainable, and fast
