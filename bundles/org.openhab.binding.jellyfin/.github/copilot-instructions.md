# GitHub Copilot Instructions for openHAB Development
These instructions are designed to guide the GitHub Copilot AI in assisting with code generation and modifications for the openHAB project. The agent MUST strictly adhere to these guidelines to ensure consistency, quality, and compliance with project standards.

## Planning Phase Clarification

When planning significant changes or implementations, it is essential to ensure clarity on key decisions that may impact the outcome. To facilitate this, the agent MUST adhere to the following guidelines:  

- **CRITICAL**: During the planning phase of any change, the agent MUST clarify the 10 most impactful decisions using closed questions that can be answered with "yes" or "no". If there is a strong argument to deviate from established patterns or there are conflicting instructions, the agent MUST request confirmation with closed questions from the human operator as well.
  
  Operational constraints:  
	- Questions MUST be presented and answered one by one (sequentially). Do NOT batch questions.
	- Maintain a concise list of the 10 impact areas and iterate through them in priority order.
	- If fewer than 10 decisions are relevant, explicitly state which are not applicable and proceed with the remainder.
	- Block further implementation steps until each question has received an explicit "yes" or "no" answer.

  ## Code Generation Guidelines

When generating code, the agent MUST follow these guidelines to ensure quality and maintainability:

- **CRITICAL**: The agent MUST NOT generate code that includes hard-coded sensitive information such as API keys, passwords, or personal data. Instead, it should use environment variables or secure vaults to manage such information.
- **CRITICAL**: The definitions available from the openHAB [guildelines]<https://www.openhab.org/docs/developer/guidelines.html> must be followed.


- Strictly follow the SOLID principles. Especially separate concerns, this also applies to private methods.
- Ensure thread safety in multi-threaded scenarios
- Consider HIPAA compliance and FDA regulations
- Prioritize code reliability and safety over performance optimizations
- Write code that is self-explanatory through good naming and clear structure

- ### Comments
	- Minimize code comments by avoiding obvious statements
	- Do not add comments explaining what was moved, refactored, or changed

- ### Logging
	- Never add logging without specific instructions to do so.
	- Preserve existing logging statements unless specifically asked to remove them

## Code Quality Gates

### Compile

- **CRITICAL**: Only use maven (`mvn`). Never use Visual Studio Code tasks or scripts to build the code.
- Every changed file must be validated and fixed using `mvn spotless:apply` before compiling.
- The added or changed code must compile without warnings

### Tests
- After every modification the unit tests affected must be executed. If new logic was added, new test must be added. All unit tests must pass. There might be integration tests that can fail. Ask, if it is unclear if a failing test is an integration test.

### Code Style
	- Added or changed code must report not code style warnings from static code analysis.

## Documentation

- Update relevant documentation to reflect code changes.

### Markdown

Entry point for documentation changes is the `docs` folder in the repository root. Follow these guidelines for markdown documentation updates:

#### Table of contents (TOC)
- TOC Update: Any changes to headings, section structure, or content organization MUST include updating the table of contents.  
- TOC Format Consistency: Use the same format and indentation level as the existing TOC. Ensure no orphaned TOC entries remain after content changes. Check that TOC structure matches document hierarchy  
- Link Fragment Validation: Ensure all TOC links match the actual heading anchors (lowercase, hyphens for spaces, special characters removed)  

#### Sections
- New Sections:
	When adding new sections, add corresponding TOC entries at the appropriate hierarchy level  
- Removed Sections:
	When removing sections, remove corresponding TOC entries  
- Section Order:
	Maintain logical section ordering in both the TOC and document structure 

#### Diagrams
- [Mermaid]<https://docs.mermaidchart.com/mermaid-oss/intro/index.html> is used to create diagrams and visualizations. See [Diagram Syntax]<https://docs.mermaidchart.com/mermaid-oss/intro/syntax-reference.html> for more information on the syntax.
- **CRITICAL**: Diagrams MUST focus on the architecture and high-level workflows. Avoid low-level implementation details.
- Leverage enhanged features like subgraphs, styling, and notes to improve clarity.
- Ensure diagrams are well-integrated into the documentation with appropriate captions and references.

## Sorce Code Management

- Follow the established branching strategy of the openHAB project.
- Commit messages MUST be clear, concise, and follow the conventional commit format.
- **CRITICAL**: Agent MUST not automatically commit changes. All changes MUST be reviewed and approved by a human developer before being committed to the repository.
- **CRITICAL**: Never add the "Close (issue-number)" statement to commit messages. This must be done by a human developer during the PR creation.