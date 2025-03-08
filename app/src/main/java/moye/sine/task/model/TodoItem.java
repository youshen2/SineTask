/*************************************************************************

 Copyright 2025 爅峫

 This file is part of KeyFinder.

 KeyFinder is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 KeyFinder is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with KeyFinder.  If not, see <http://www.gnu.org/licenses/>.

 *************************************************************************/
package moye.sine.task.model;

import java.util.ArrayList;
import java.util.List;

public class TodoItem {
    private long id;
    private String content;
    private long time;
    private boolean isCompleted;
    private long parentId;
    private List<TodoItem> children = new ArrayList<>();
    private boolean isExpanded;

    public TodoItem(long id, String content, long time, boolean isCompleted, long parentId) {
        this.id = id;
        this.content = content;
        this.time = time;
        this.isCompleted = isCompleted;
        this.parentId = parentId;
    }

    public boolean hasUncompletedChildren() {
        for (TodoItem child : children) {
            if (!child.isCompleted() || child.hasUncompletedChildren()) {
                return true;
            }
        }
        return false;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public long getTime() {
        return time;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public long getParentId() {
        return parentId;
    }

    public List<TodoItem> getChildren() {
        return children;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public void addChild(TodoItem child) {
        children.add(child);
    }

    public void setContent(String content) {
        this.content = content;
    }
}