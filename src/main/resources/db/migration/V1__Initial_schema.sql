-- V1__Initial_schema.sql
-- Email Templates table
CREATE TABLE email_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    subject_template VARCHAR(500) NOT NULL,
    body_template TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Email Queue table
CREATE TABLE email_queue (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_id UUID REFERENCES email_templates(id),
    recipient VARCHAR(255) NOT NULL,
    cc VARCHAR(1000),
    bcc VARCHAR(1000),
    subject VARCHAR(500) NOT NULL,
    body TEXT NOT NULL,
    variables JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT DEFAULT 0,
    error_message TEXT,
    scheduled_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Email Delivery Log table
CREATE TABLE email_delivery_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email_id UUID NOT NULL REFERENCES email_queue(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_email_queue_status ON email_queue(status);
CREATE INDEX idx_email_queue_scheduled_at ON email_queue(scheduled_at);
CREATE INDEX idx_email_queue_created_at ON email_queue(created_at);
CREATE INDEX idx_email_delivery_log_email_id ON email_delivery_log(email_id);

-- Insert default templates
INSERT INTO email_templates (name, subject_template, body_template) VALUES
('welcome', 'Welcome to {{appName}}!', '<html><body><h1>Welcome, {{userName}}!</h1><p>Thank you for joining {{appName}}. We are excited to have you on board.</p></body></html>'),
('password-reset', 'Password Reset Request', '<html><body><h1>Password Reset</h1><p>Hi {{userName}},</p><p>Click <a href="{{resetLink}}">here</a> to reset your password. This link expires in {{expiryHours}} hours.</p></body></html>'),
('notification', '{{subject}}', '<html><body><p>{{message}}</p></body></html>');
