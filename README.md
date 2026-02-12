## KanbanCLI – Manage tasks from your terminal

KanbanCLI is a simple command-line tool for managing tasks using a Kanban-style workflow.  
It helps you organize tasks into statuses such as **Todo**, **In Progress**, and **Done**, and move them directly from your terminal.

## Features

- Create and manage task collections
- Organize tasks using Kanban statuses
- Move tasks between statuses
- Set due dates for tasks
- Fully terminal-based workflow

## Usage

`kanban [command] [options]`

To see all available commands and options:

`kanban --help`

## Commands

- `new` – Create a new collection or task
    
- `use` – Select a collection to work with (collection only)
    
- `list` – List collections or tasks
    
- `move` – Move a task from one status to another
    
- `delete` – Delete a task or collection
    
- `duedate` – Set or remove a due date from a task (task only)
    
- `rename` – Rename a collection

- `cleanup` - Remove expired tasks

## Rules and constraints

- A collection name must be unique.
- A task name must be unique within the same collection.
- Dates must follow the format `DD/MM/YYYY`.

## Task statuses

KanbanCLI uses the following task statuses:

- `todo`
- `in_progress`
- `done`

## Example workflow

Create a new collection and start working on it:
```
kanban new collection my-project
kanban use my-project
```

Create a task:
```
kanban new task "Implement login"
```

Move a task to another status:
```
kanban move in_progress "Implement login"
```

Move all tasks from one status to another:
```
kanban move --all in_progress(old status) done(new status)
```

Set a due date (format: dd/MM/yyyy):
```
kanban duedate "Implement login" --set 12/12/2026
```

Remove a due date:
```
kanban duedate "Implement login" --remove
```

Rename a collection or task:
```
kanban rename collection my-project My Project
kanban rename task "Implement login" "Implement login page"
```

List collections or tasks:
```
kanban list collection
kanban list task
```

List tasks in a specific status:
```
kanban list task --status todo
```

Delete a collection or task:
```
kanban delete collection my-project
kanban delete task "Implement login"
```

Delete all tasks from a status:
```
kanban delete task --all --status done
```

Delete all collections or tasks:
```
kanban delete collection --all
kanban delete task --all
```

Remove all expired tasks or expired tasks from a specific status:
```
kanban cleanup --all
kanban cleanup --status done
```